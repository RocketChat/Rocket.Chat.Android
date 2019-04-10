package chat.rocket.android.server.infraestructure

import androidx.lifecycle.MutableLiveData
import chat.rocket.android.db.DatabaseManager
import chat.rocket.common.model.BaseRoom
import chat.rocket.common.model.User
import chat.rocket.common.model.UserStatus
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.realtime.setDefaultStatus
import chat.rocket.core.internal.realtime.setTemporaryStatus
import chat.rocket.core.internal.realtime.socket.connect
import chat.rocket.core.internal.realtime.socket.disconnect
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.internal.realtime.socket.model.StreamMessage
import chat.rocket.core.internal.realtime.socket.model.Type
import chat.rocket.core.internal.realtime.subscribeActiveUsers
import chat.rocket.core.internal.realtime.subscribeRoomMessages
import chat.rocket.core.internal.realtime.subscribeRooms
import chat.rocket.core.internal.realtime.subscribeSubscriptions
import chat.rocket.core.internal.realtime.subscribeUserData
import chat.rocket.core.internal.realtime.unsubscribe
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.Message
import chat.rocket.core.model.Myself
import chat.rocket.core.model.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.selects.select
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.coroutines.CoroutineContext

class ConnectionManager(
    internal val client: RocketChatClient,
    private val dbManager: DatabaseManager
) : CoroutineScope {
    private var connectJob : Job? = null
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    val statusLiveData = MutableLiveData<State>()
    private val statusChannelList = CopyOnWriteArrayList<Channel<State>>()
    private val statusChannel = Channel<State>(Channel.CONFLATED)

    private val roomMessagesChannels = LinkedHashMap<String, Channel<Message>>()
    private val userDataChannels = ArrayList<Channel<Myself>>()
    private val roomsChannels = LinkedHashMap<String, Channel<Room>>()
    private val subscriptionIdMap = HashMap<String, String>()

    private var subscriptionId: String? = null
    private var roomsId: String? = null
    private var userDataId: String? = null
    private var activeUserId: String? = null
    private var temporaryStatus: UserStatus? = null

    private val activeUsersContext = newSingleThreadContext("activeUsersContext")
    private val roomsContext = newSingleThreadContext("roomsContext")
    private val messagesContext = newSingleThreadContext("messagesContext")

    fun connect() {
        if (connectJob?.isActive == true && state !is State.Disconnected) {
            Timber.d("Already connected, just returning...")
            return
        }

        // cleanup first
        client.removeStateChannel(statusChannel)
        client.disconnect()
        connectJob?.cancel()

        // Connect and setup
        client.addStateChannel(statusChannel)
        connectJob = launch {
            for (status in statusChannel) {
                Timber.d("Changing status to: $status")
                when (status) {
                    is State.Connected -> {
                        dbManager.clearUsersStatus()

                        client.subscribeSubscriptions { _, id ->
                            Timber.d("Subscribed to subscriptions: $id")
                            subscriptionId = id
                        }

                        client.subscribeRooms { _, id ->
                            Timber.d("Subscribed to rooms: $id")
                            roomsId = id
                        }

                        client.subscribeUserData { _, id ->
                            Timber.d("Subscribed to the userData id: $id")
                            userDataId = id
                        }

                        client.subscribeActiveUsers { _, id ->
                            Timber.d("Subscribed to the activeUser id: $id")
                            activeUserId = id
                        }

                        resubscribeRooms()
                        temporaryStatus?.let { client.setTemporaryStatus(it) }

                    }
                    is State.Waiting -> Timber.d("Connection in: ${status.seconds}")
                }

                statusLiveData.postValue(status)

                for (channel in statusChannelList) {
                    Timber.d("Sending status: $status to $channel")
                    channel.offer(status)
                }
            }
        }

        var totalBatchedUsers = 0
        val userActor = createBatchActor<User>(
            activeUsersContext, parent = connectJob, maxSize = 500, maxTime = 1000
        ) { users ->
            totalBatchedUsers += users.size
            Timber.d("Processing Users batch: ${users.size} - $totalBatchedUsers")

            // TODO - move this to an Interactor
            dbManager.processUsersBatch(users)
        }

        val roomsActor = createBatchActor<StreamMessage<BaseRoom>>(
            roomsContext, parent = connectJob, maxSize = 10
        ) { batch ->
            Timber.d("processing Stream batch: ${batch.size} - $batch")
            dbManager.processChatRoomsBatch(batch)

            batch.forEach {
                //TODO - Do we need to handle Type.Removed and Type.Inserted here?
                if (it.type == Type.Updated) {
                    if (it.data is Room) {
                        val room = it.data as Room
                        roomsChannels[it.data.id]?.offer(room)
                    }
                }
            }
        }

        val messagesActor = createBatchActor<Message>(
            messagesContext, parent = connectJob, maxSize = 100, maxTime = 500
        ) { messages ->
            Timber.d("Processing Messages batch: ${messages.size}")
            dbManager.processMessagesBatch(messages.distinctBy { it.id })

            launch {
                messages.forEach { message ->
                    val channel = roomMessagesChannels[message.roomId]
                    channel?.send(message)
                }
            }
        }

        // stream-notify-user - ${userId}/rooms-changed
        launch {
            for (room in client.roomsChannel) {
                Timber.d("GOT Room streamed")
                roomsActor.send(room)
                if (room.type != Type.Removed) {
                    room.data.lastMessage?.let {
                        messagesActor.send(it)
                    }
                }
            }
        }

        // stream-notify-user - ${userId}/subscriptions-changed
        launch {
            for (subscription in client.subscriptionsChannel) {
                Timber.d("GOT Subscription streamed")
                roomsActor.send(subscription)
            }
        }

        // stream-room-messages - $roomId
        launch {
            for (message in client.messagesChannel) {
                Timber.d("Received new Message for room ${message.roomId}")
                messagesActor.send(message)
            }
        }

        // userData
        launch {
            for (myself in client.userDataChannel) {
                Timber.d("Got userData")
                dbManager.updateSelfUser(myself)
                for (channel in userDataChannels) {
                    channel.send(myself)
                }
            }
        }

        // activeUsers
        launch {
            for (user in client.activeUsersChannel) {
                userActor.send(user)
            }
        }

        client.connect()

        // Broadcast initial state...
        val state = client.state
        for (channel in statusChannelList) {
            channel.offer(state)
        }
    }

    fun setDefaultStatus(userStatus: UserStatus) {
        temporaryStatus = null
        client.setDefaultStatus(userStatus)
    }

    fun setTemporaryStatus(userStatus: UserStatus) {
        temporaryStatus = userStatus
        client.setTemporaryStatus(userStatus)
    }

    fun resetReconnectionTimer() {
        // if we are waiting to reconnect, immediately try to reconnect
        // and reset the reconnection counter
        if (client.state is State.Waiting) {
            client.connect(resetCounter = true)
        }
    }

    private fun resubscribeRooms() {
        roomMessagesChannels.toList().map { (roomId, _) ->
            client.subscribeRoomMessages(roomId) { _, id ->
                Timber.d("Subscribed to $roomId: $id")
                subscriptionIdMap[roomId] = id
            }
        }
    }

    fun disconnect() {
        Timber.d("ConnectionManager DISCONNECT")
        client.removeStateChannel(statusChannel)
        client.disconnect()
        connectJob?.cancel()
        temporaryStatus = null
    }

    fun addStatusChannel(channel: Channel<State>) = statusChannelList.add(channel)

    fun removeStatusChannel(channel: Channel<State>) = statusChannelList.remove(channel)

    fun addUserDataChannel(channel: Channel<Myself>) = userDataChannels.add(channel)

    fun removeUserDataChannel(channel: Channel<Myself>) = userDataChannels.remove(channel)

    fun addRoomChannel(roomId: String, channel: Channel<Room>) {
        roomsChannels[roomId] = channel
    }

    fun removeRoomChannel(roomId: String) {
        roomsChannels.remove(roomId)
    }

    fun subscribeRoomMessages(roomId: String, channel: Channel<Message>) {
        val oldSub = roomMessagesChannels.put(roomId, channel)
        if (oldSub != null) {
            Timber.d("Room $roomId already subscribed...")
            return
        }

        if (client.state is State.Connected) {
            client.subscribeRoomMessages(roomId) { _, id ->
                Timber.d("Subscribed to $roomId: $id")
                subscriptionIdMap[roomId] = id
            }
        }
    }

    fun unsubscribeRoomMessages(roomId: String) {
        val sub = roomMessagesChannels.remove(roomId)
        if (sub != null) {
            val id = subscriptionIdMap.remove(roomId)
            id?.let { client.unsubscribe(it) }
        }
    }

    private inline fun <T> createBatchActor(
        context: CoroutineContext = Dispatchers.IO,
        parent: Job? = null,
        maxSize: Int = 100,
        maxTime: Int = 500,
        crossinline block: (List<T>) -> Unit
    ): SendChannel<T> {
        return actor(context) {
            val batch = ArrayList<T>(maxSize)
            var deadline = 0L // deadline for sending this batch to callback block

            while (true) {
                // when deadline is reached or size is exceeded, pass the batch to the callback block
                val remainingTime = deadline - System.currentTimeMillis()
                if (batch.isNotEmpty() && remainingTime <= 0 || batch.size >= maxSize) {
                    Timber.d("Processing batch: ${batch.size}")
                    block(batch.toList())
                    batch.clear()
                    continue
                }

                // wait until items is received or timeout reached
                select<Unit> {
                    // when received -> add to batch
                    channel.onReceive {
                        batch.add(it)
                        //Timber.d("Adding user to batch: ${batch.size}")
                        // init deadline on first item added to batch
                        if (batch.size == 1) deadline = System.currentTimeMillis() + maxTime
                    }
                    // when timeout is reached just finish select, note: no timeout when batch is empty
                    if (batch.isNotEmpty()) onTimeout(remainingTime.orZero()) {}
                }

                if (!isActive) break
            }
        }
    }
}

private fun Long.orZero(): Long {
    return if (this < 0) 0 else this
}

suspend fun ConnectionManager.chatRooms(timestamp: Long = 0, filterCustom: Boolean = true) =
    client.chatRooms(timestamp, filterCustom)

val ConnectionManager.state: State
    get() = client.state