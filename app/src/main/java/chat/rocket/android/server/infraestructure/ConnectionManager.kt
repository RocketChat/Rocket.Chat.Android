package chat.rocket.android.server.infraestructure

import chat.rocket.common.model.BaseRoom
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.realtime.*
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.Message
import chat.rocket.core.model.Myself
import kotlinx.coroutines.experimental.Job
import kotlinx.coroutines.experimental.channels.Channel
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import java.util.concurrent.CopyOnWriteArrayList


class ConnectionManager(internal val client: RocketChatClient) {
    private val statusChannelList = CopyOnWriteArrayList<Channel<State>>()
    private val statusChannel = Channel<State>(Channel.CONFLATED)
    private var connectJob: Job? = null

    private val roomAndSubscriptionChannels = ArrayList<Channel<StreamMessage<BaseRoom>>>()
    private val roomMessagesChannels = LinkedHashMap<String, Channel<Message>>()
    private val userDataChannels = ArrayList<Channel<Myself>>()
    private val subscriptionIdMap = HashMap<String, String>()

    private var subscriptionId: String? = null
    private var roomsId: String? = null
    private var userId: String? = null

    fun connect() {
        if (connectJob?.isActive == true && (state !is State.Disconnected)) {
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
                        client.subscribeSubscriptions { _, id ->
                            Timber.d("Subscribed to subscriptions: $id")
                            subscriptionId = id
                        }
                        client.subscribeRooms { _, id ->
                            Timber.d("Subscribed to rooms: $id")
                            roomsId = id
                        }

                        client.subscribeUserDataChanges { _, id ->
                            Timber.d("Subscribed to the user: $id")
                            userId = id
                        }

                        resubscribeRooms()
                    }
                    is State.Waiting -> {
                        Timber.d("Connection in: ${status.seconds}")
                    }
                }

                for (channel in statusChannelList) {
                    Timber.d("Sending status: $status to $channel")
                    channel.offer(status)
                }
            }
        }

        launch(parent = connectJob) {
            for (room in client.roomsChannel) {
                Timber.d("GOT Room streamed")
                for (channel in roomAndSubscriptionChannels) {
                    channel.send(room)
                }
            }
        }

        launch(parent = connectJob) {
            for (subscription in client.subscriptionsChannel) {
                Timber.d("GOT Subscription streamed")
                for (channel in roomAndSubscriptionChannels) {
                    channel.send(subscription)
                }
            }
        }

        launch(parent = connectJob) {
            for (message in client.messagesChannel) {
                Timber.d("Received new Message for room ${message.roomId}")
                val channel = roomMessagesChannels[message.roomId]
                channel?.send(message)

            }
        }

        launch(parent = connectJob) {
            for (myself in client.userDataChannel) {
                Timber.d("Got userData")
                for (channel in userDataChannels) {
                    channel.send(myself)
                }
            }
        }

        client.connect()

        // Broadcast initial state...
        val state = client.state
        for (channel in statusChannelList) {
            channel.offer(state)
        }
    }

    private fun resubscribeRooms() {
        roomMessagesChannels.toList().map { (roomId, channel) ->
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
    }

    fun addStatusChannel(channel: Channel<State>) = statusChannelList.add(channel)

    fun removeStatusChannel(channel: Channel<State>) = statusChannelList.remove(channel)

    fun addRoomsAndSubscriptionsChannel(channel: Channel<StreamMessage<BaseRoom>>) = roomAndSubscriptionChannels.add(channel)

    fun removeRoomsAndSubscriptionsChannel(channel: Channel<StreamMessage<BaseRoom>>) = roomAndSubscriptionChannels.remove(channel)

    fun addUserDataChannel(channel: Channel<Myself>) = userDataChannels.add(channel)

    fun removeUserDataChannel(channel: Channel<Myself>) = userDataChannels.remove(channel)

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
}

suspend fun ConnectionManager.chatRooms(timestamp: Long = 0, filterCustom: Boolean = true)
        = client.chatRooms(timestamp, filterCustom)

val ConnectionManager.state: State
    get() = client.state