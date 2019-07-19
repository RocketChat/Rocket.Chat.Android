package chat.rocket.android.server.infrastructure

import androidx.lifecycle.MutableLiveData
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.util.extension.orZero
import chat.rocket.common.model.BaseRoom
import chat.rocket.common.model.User
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.realtime.socket.connect
import chat.rocket.core.internal.realtime.socket.disconnect
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.internal.realtime.socket.model.StreamMessage
import chat.rocket.core.internal.realtime.socket.model.Type
import chat.rocket.core.internal.realtime.subscribeActiveUsers
import chat.rocket.core.internal.realtime.subscribeRoomMessages
import chat.rocket.core.internal.realtime.subscribeRooms
import chat.rocket.core.internal.realtime.subscribeSubscriptions
import chat.rocket.core.internal.realtime.unsubscribe
import chat.rocket.core.model.Message
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
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO

    private var connectJob: Job? = null
    private val activeUsersContext = newSingleThreadContext("activeUsersContext")
    private val roomsContext = newSingleThreadContext("roomsContext")

    val stateLiveData = MutableLiveData<State>()
    private val stateChannel = Channel<State>()
    private val stateChannelList = CopyOnWriteArrayList<Channel<State>>()

    private val roomsChannels = LinkedHashMap<String, Channel<Room>>()

    fun connect() {
        if (connectJob?.isActive == true && client.state !is State.Disconnected) {
            Timber.d("Already connected")
            return
        }

        // Cleanup first
        disconnect()

        // Setup and connect
        client.addStateChannel(stateChannel)
        connectJob = launch {
            for (state in stateChannel) {
                Timber.d("Changing state to: $state")

                if (state is State.Connected) {
                    client.subscribeSubscriptions { _, _ -> }
                    client.subscribeRooms { _, _ -> }
                    client.subscribeActiveUsers { _, _ -> }
                }

                stateLiveData.postValue(state)
                stateChannelList.forEach { it.offer(state) }
            }
        }

        addElementsIntoChannel()
        client.connect()
    }

    fun disconnect() {
        Timber.d("Disconnecting")
        connectJob?.cancel()
        with(client) {
            removeStateChannel(stateChannel)
            disconnect()
        }
        Timber.d("Disconnected")
    }

    fun resetReconnectionTimer() {
        // if we're waiting to reconnect immediately try to reconnect and reset the reconnection counter.
        if (client.state is State.Waiting) {
            client.connect(resetCounter = true)
        }
    }

    private fun addElementsIntoChannel() {
        val roomsActor = createBatchActor<StreamMessage<BaseRoom>>(
            roomsContext,
            maxSize = 10
        ) { batch ->
            Timber.d("Processing stream batch: ${batch.size} - $batch")
            dbManager.processChatRoomsBatch(batch)

            batch.forEach {
                // TODO - Do we need to handle Type.Removed and Type.Inserted here?
                if (it.type == Type.Updated) {
                    if (it.data is Room) {
                        val room = it.data as Room
                        roomsChannels[it.data.id]?.offer(room)
                    }
                }
            }
        }

        var totalBatchedUsers = 0
        val userActor = createBatchActor<User>(
            activeUsersContext,
            maxSize = 500,
            maxTime = 1000
        ) { users ->
            totalBatchedUsers += users.size
            Timber.d("Processing users batch: ${users.size} - $totalBatchedUsers")
            dbManager.processUsersBatch(users)
        }

        launch {
            for (room in client.roomsChannel) {
                Timber.d("Got room streamed")
                roomsActor.send(room)
            }

            for (subscription in client.subscriptionsChannel) {
                Timber.d("Got subscription streamed")
                roomsActor.send(subscription)
            }

            for (user in client.activeUsersChannel) {
                userActor.send(user)
            }
        }
    }

    fun addStateChannel(channel: Channel<State>) = stateChannelList.add(channel)

    fun removeStateChannel(channel: Channel<State>) = stateChannelList.remove(channel)

    fun addRoomChannel(roomId: String, channel: Channel<Room>) {
        roomsChannels[roomId] = channel
    }

    fun removeRoomChannel(roomId: String) = roomsChannels.remove(roomId)

    private inline fun <T> createBatchActor(
        context: CoroutineContext = Dispatchers.IO,
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