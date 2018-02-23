package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.SaveChatRoomsInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.model.Subscription
import chat.rocket.core.internal.realtime.*
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Room
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.channels.Channel
import timber.log.Timber
import javax.inject.Inject

class ChatRoomsPresenter @Inject constructor(private val view: ChatRoomsView,
                                             private val strategy: CancelStrategy,
                                             private val navigator: ChatRoomsNavigator,
                                             private val serverInteractor: GetCurrentServerInteractor,
                                             private val getChatRoomsInteractor: GetChatRoomsInteractor,
                                             private val saveChatRoomsInteractor: SaveChatRoomsInteractor,
                                             private val refreshSettingsInteractor: RefreshSettingsInteractor,
                                             factory: RocketChatClientFactory) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)
    private val currentServer = serverInteractor.get()!!
    private var reloadJob: Deferred<List<ChatRoom>>? = null

    private val stateChannel = Channel<State>()

    fun loadChatRooms() {
        refreshSettingsInteractor.refreshAsync(currentServer)
        launchUI(strategy) {
            view.showLoading()
            try {
                view.updateChatRooms(loadRooms())
                subscribeRoomUpdates()
            } catch (e: RocketChatException) {
                Timber.e(e)
                view.showMessage(e.message!!)
            } finally {
                view.hideLoading()
            }
        }
    }

    fun loadChatRoom(chatRoom: ChatRoom) = navigator.toChatRoom(chatRoom.id, chatRoom.name,
            chatRoom.type.toString(), chatRoom.readonly ?: false)

    /**
     * Gets a [ChatRoom] list from local repository.
     * ChatRooms returned are filtered by name.
     */
    fun chatRoomsByName(name: String) {
        val currentServer = serverInteractor.get()!!
        launchUI(strategy) {
            val roomList = getChatRoomsInteractor.getByName(currentServer, name)
            view.updateChatRooms(roomList)
        }
    }

    private suspend fun loadRooms(): List<ChatRoom> {
        val chatRooms = client.chatRooms().update
        val sortedRooms = sortRooms(chatRooms)
        saveChatRoomsInteractor.save(currentServer, sortedRooms)
        return sortedRooms
    }

    private fun sortRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        val openChatRooms = getOpenChatRooms(chatRooms)
        return sortChatRooms(openChatRooms)
    }

    private fun updateRooms() {
        launch {
            view.updateChatRooms(getChatRoomsInteractor.get(currentServer))
        }
    }

    private fun getOpenChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter(ChatRoom::open)
    }

    private fun sortChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.sortedByDescending { chatRoom ->
            chatRoom.lastMessage?.timestamp
        }
    }

    // TODO - Temporary stuff, remove when adding DB support
    private suspend fun subscribeRoomUpdates() {
        client.addStateChannel(stateChannel)
        launch(CommonPool + strategy.jobs) {
            for (status in stateChannel) {
                Timber.d("Changing status to: $status")
                when (status) {
                    State.Authenticating -> Timber.d("Authenticating")
                    State.Connected -> {
                        Timber.d("Connected")
                        client.subscribeSubscriptions {
                            Timber.d("subscriptions: $it")
                        }
                        client.subscribeRooms {
                            Timber.d("rooms: $it")
                        }
                    }
                }
            }
            Timber.d("Done on statusChannel")
        }

        when (client.state) {
            State.Connected -> {
                Timber.d("Already connected")
            }
            else -> client.connect()
        }

        launch(CommonPool + strategy.jobs) {
            for (message in client.roomsChannel) {
                Timber.d("Got message: $message")
                updateRoom(message)
            }
        }

        launch(CommonPool + strategy.jobs) {
            for (message in client.subscriptionsChannel) {
                Timber.d("Got message: $message")
                updateSubscription(message)
            }
        }
    }

    private fun updateRoom(message: StreamMessage<Room>) {
        launchUI(strategy) {
            when (message.type) {
                Type.Removed -> {
                    removeRoom(message.data.id)
                }
                Type.Updated -> {
                    updateRoom(message.data)
                }
                Type.Inserted -> {
                    // On insertion, just get all chatrooms again, since we can't create one just
                    // from a Room
                    reloadRooms()
                }
            }

            updateRooms()
        }
    }

    private fun updateSubscription(message: StreamMessage<Subscription>) {
        launchUI(strategy) {
            when (message.type) {
                Type.Removed -> {
                    removeRoom(message.data.roomId)
                }
                Type.Updated -> {
                    updateSubscription(message.data)
                }
                Type.Inserted -> {
                    // On insertion, just get all chatrooms again, since we can't create one just
                    // from a Subscription
                    reloadRooms()
                }
            }

            updateRooms()
        }
    }

    private suspend fun reloadRooms() {
        Timber.d("realoadRooms()")
        reloadJob?.cancel()

        reloadJob = async(CommonPool + strategy.jobs) {
            delay(1000)
            Timber.d("reloading rooms after wait")
            loadRooms()
        }
        reloadJob?.await()
    }

    // Update a ChatRoom with a Room information
    private fun updateRoom(room: Room) {
        val chatRooms = getChatRoomsInteractor.get(currentServer).toMutableList()
        val chatRoom = chatRooms.find { chatRoom -> chatRoom.id == room.id }
        chatRoom?.apply {
            val newRoom = ChatRoom(room.id,
                    room.type,
                    room.user ?: user,
                    room.name ?: name,
                    room.fullName ?: fullName,
                    room.readonly,
                    room.updatedAt ?: updatedAt,
                    timestamp,
                    lastSeen,
                    room.topic,
                    room.announcement,
                    default,
                    open,
                    alert,
                    unread,
                    userMenstions,
                    groupMentions,
                    room.lastMessage,
                    client)
            removeRoom(room.id, chatRooms)
            chatRooms.add(newRoom)
            saveChatRoomsInteractor.save(currentServer, sortRooms(chatRooms))
        }
    }

    // Update a ChatRoom with a Subscription information
    private fun updateSubscription(subscription: Subscription) {
        val chatRooms = getChatRoomsInteractor.get(currentServer).toMutableList()
        val chatRoom = chatRooms.find { chatRoom -> chatRoom.id == subscription.roomId }
        chatRoom?.apply {
            val newRoom = ChatRoom(subscription.roomId,
                    subscription.type,
                    subscription.user ?: user,
                    subscription.name,
                    subscription.fullName ?: fullName,
                    subscription.readonly ?: readonly,
                    subscription.updatedAt ?: updatedAt,
                    subscription.timestamp ?: timestamp,
                    subscription.lastSeen ?: lastSeen,
                    topic,
                    announcement,
                    subscription.isDefault,
                    subscription.open,
                    subscription.alert,
                    subscription.unread,
                    subscription.userMentions,
                    subscription.groupMentions,
                    lastMessage,
                    client)
            removeRoom(subscription.roomId, chatRooms)
            chatRooms.add(newRoom)
            saveChatRoomsInteractor.save(currentServer, sortRooms(chatRooms))
        }
    }


    private fun removeRoom(id: String,
                           chatRooms: MutableList<ChatRoom> = getChatRoomsInteractor.get(currentServer).toMutableList()) {
        synchronized(this) {
            chatRooms.removeAll { chatRoom -> chatRoom.id == id }
        }
        saveChatRoomsInteractor.save(currentServer, sortRooms(chatRooms))
    }

    fun disconnect() {
        client.removeStateChannel(stateChannel)
        client.disconnect()
    }
}