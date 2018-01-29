package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.SaveChatRoomsInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.model.Subscription
import chat.rocket.core.internal.realtime.*
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Room
import kotlinx.coroutines.experimental.*
import timber.log.Timber
import javax.inject.Inject

class ChatRoomsPresenter @Inject constructor(private val view: ChatRoomsView,
                                             private val strategy: CancelStrategy,
                                             private val navigator: ChatRoomsNavigator,
                                             serverInteractor: GetCurrentServerInteractor,
                                             private val getChatRoomsInteractor: GetChatRoomsInteractor,
                                             private val saveChatRoomsInteractor: SaveChatRoomsInteractor,
                                             factory: RocketChatClientFactory) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)
    private val currentServer = serverInteractor.get()!!
    private var reloadJob: Deferred<List<ChatRoom>?>? = null

    fun loadChatRooms() {
        launchUI(strategy) {
            view.showLoading()
            try {
                val chatRooms = getChatRooms()
                if (chatRooms != null) {
                    view.updateChatRooms(chatRooms)
                    subscribeRoomUpdates()
                } else {
                    view.showNoChatRoomsToDisplay()
                }
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            }
            view.hideLoading()
        }
    }

    fun loadChatRoom(chatRoom: ChatRoom) {
        navigator.toChatRoom(chatRoom.id,
                chatRoom.name,
                chatRoom.type.name,
                chatRoom.readonly ?: false)
    }

    /**
     * Gets a [ChatRoom] list filtered by name from local repository.
     *
     * @param name The Chat Room name to get.
     */
    fun chatRoomsByName(name: String) {
        launchUI(strategy) {
            val roomList = getChatRoomsInteractor.getByName(currentServer, name)
            view.updateChatRooms(roomList)
        }
    }

    private suspend fun getChatRooms(): List<ChatRoom>? {
        val chatRooms = client.chatRooms().update
        if (chatRooms != null) {
            val sortedOpenChatRooms = sortOpenChatRooms(chatRooms)
            saveChatRoomsInteractor.save(currentServer, sortedOpenChatRooms)
            return sortedOpenChatRooms
        }
        return null
    }

    private fun sortOpenChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        val openChatRooms = getOpenChatRooms(chatRooms)
        return sortChatRooms(openChatRooms)
    }

    private fun getOpenChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter(ChatRoom::open)
    }

    private fun sortChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.sortedByDescending { chatRoom ->
            chatRoom.lastMessage?.timestamp
        }
    }
    private fun updateChatRooms() {
        launch {
            view.updateChatRooms(getChatRoomsInteractor.get(currentServer))
        }
    }

    // TODO - Temporary stuff, remove when adding DB support
    private suspend fun subscribeRoomUpdates() {
        launch(CommonPool + strategy.jobs) {
            for (status in client.statusChannel) {
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

            updateChatRooms()
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

            updateChatRooms()
        }
    }

    private suspend fun reloadRooms() {
        Timber.d("realoadRooms()")
        reloadJob?.cancel()

        reloadJob = async(CommonPool + strategy.jobs) {
            delay(1000)
            Timber.d("reloading rooms after wait")
            getChatRooms()
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
                    lastModified,
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
            saveChatRoomsInteractor.save(currentServer, sortOpenChatRooms(chatRooms))
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
                    subscription.lastModified ?: lastModified,
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
            saveChatRoomsInteractor.save(currentServer, sortOpenChatRooms(chatRooms))
        }
    }


    private fun removeRoom(id: String,
                           chatRooms: MutableList<ChatRoom> = getChatRoomsInteractor.get(currentServer).toMutableList()) {
        synchronized(this) {
            chatRooms.removeAll { chatRoom -> chatRoom.id == id }
        }
        saveChatRoomsInteractor.save(currentServer, sortOpenChatRooms(chatRooms))
    }

    fun disconnect() = client.disconnect()
}