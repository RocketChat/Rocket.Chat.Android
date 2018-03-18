package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.chatRooms
import chat.rocket.android.server.infraestructure.state
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.*
import chat.rocket.core.internal.model.Subscription
import chat.rocket.core.internal.realtime.State
import chat.rocket.core.internal.realtime.StreamMessage
import chat.rocket.core.internal.realtime.Type
import chat.rocket.core.internal.rest.spotlight
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Room
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import timber.log.Timber
import javax.inject.Inject

class ChatRoomsPresenter @Inject constructor(private val view: ChatRoomsView,
                                             private val strategy: CancelStrategy,
                                             private val navigator: MainNavigator,
                                             private val serverInteractor: GetCurrentServerInteractor,
                                             private val getChatRoomsInteractor: GetChatRoomsInteractor,
                                             private val saveChatRoomsInteractor: SaveChatRoomsInteractor,
                                             private val refreshSettingsInteractor: RefreshSettingsInteractor,
                                             settingsRepository: SettingsRepository,
                                             factory: ConnectionManagerFactory) {
    private val manager: ConnectionManager = factory.create(serverInteractor.get()!!)
    private val currentServer = serverInteractor.get()!!
    private val client = manager.client
    private var reloadJob: Deferred<List<ChatRoom>>? = null
    private val settings = settingsRepository.get(currentServer)!!

    private val subscriptionsChannel = Channel<StreamMessage<BaseRoom>>()
    private val stateChannel = Channel<State>()

    private var lastState = manager.state

    fun loadChatRooms() {
        refreshSettingsInteractor.refreshAsync(currentServer)
        launchUI(strategy) {
            view.showLoading()
            subscribeStatusChange()
            try {
                view.updateChatRooms(loadRooms())
            } catch (e: RocketChatException) {
                Timber.e(e)
                view.showMessage(e.message!!)
            } finally {
                view.hideLoading()
            }
            subscribeRoomUpdates()
        }
    }

    fun loadChatRoom(chatRoom: ChatRoom) {
        val roomName = if (chatRoom.type is RoomType.DirectMessage
                && chatRoom.fullName != null
                && settings.useRealName()) {
            chatRoom.fullName!!
        } else {
            chatRoom.name
        }

        navigator.toChatRoom(chatRoom.id, roomName,
                chatRoom.type.toString(), chatRoom.readonly ?: false,
                chatRoom.lastSeen ?: -1,
                chatRoom.open)
    }

    /**
     * Gets a [ChatRoom] list from local repository.
     * ChatRooms returned are filtered by name.
     */
    fun chatRoomsByName(name: String) {
        val currentServer = serverInteractor.get()!!
        launchUI(strategy) {
            try {
                val roomList = getChatRoomsInteractor.getByName(currentServer, name)
                if (roomList.isEmpty()) {
                    val (users, rooms) = client.spotlight(name)
                    val chatRoomsCombined = mutableListOf<ChatRoom>()
                    chatRoomsCombined.addAll(usersToChatRooms(users))
                    chatRoomsCombined.addAll(roomsToChatRooms(rooms))
                    view.updateChatRooms(chatRoomsCombined)
                } else {
                    view.updateChatRooms(roomList)
                }
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    private suspend fun usersToChatRooms(users: List<User>): List<ChatRoom> {
        return users.map {
            ChatRoom(it.id, RoomType.DIRECT_MESSAGE, SimpleUser(
                    username = it.username, name = it.name, id = null), it.name ?: "",
                    it.name, false, null, null, null,
                    null, null, false, false, false,
                    0L, null, 0L, null, client
            )
        }
    }

    private suspend fun roomsToChatRooms(rooms: List<Room>): List<ChatRoom> {
        return rooms.map {
            ChatRoom(it.id, it.type, it.user, it.name ?: "",
                    it.fullName, it.readonly, it.updatedAt, null, null,
                    it.topic, it.announcement, false, false, false,
                    0L, null, 0L, it.lastMessage, client
            )
        }
    }

    private suspend fun loadRooms(): List<ChatRoom> {
        val chatRooms = manager.chatRooms().update
        val sortedRooms = sortRooms(chatRooms)
        Timber.d("Loaded rooms: ${sortedRooms.size}")
        saveChatRoomsInteractor.save(currentServer, sortedRooms)
        return sortedRooms
    }

    private fun sortRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        val openChatRooms = getOpenChatRooms(chatRooms)
        return sortChatRooms(openChatRooms)
    }

    private fun updateRooms() {
        Timber.d("Updating Rooms")
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

    private suspend fun subscribeStatusChange() {
        lastState = manager.state
        launch(CommonPool + strategy.jobs) {
            for (state in stateChannel) {
                Timber.d("Got new state: $state - last: $lastState")
                if (state != lastState) {
                    launch(UI) {
                        view.showConnectionState(state)
                    }

                    if (state is State.Connected) {
                        reloadRooms()
                        updateRooms()
                    }
                }
                lastState = state
            }
        }
    }

    // TODO - Temporary stuff, remove when adding DB support
    private suspend fun subscribeRoomUpdates() {
        manager.addStatusChannel(stateChannel)
        manager.addRoomsAndSubscriptionsChannel(subscriptionsChannel)
        launch(CommonPool + strategy.jobs) {
            for (message in subscriptionsChannel) {
                Timber.d("Got message: $message")
                when (message.data) {
                    is Room -> updateRoom(message as StreamMessage<Room>)
                    is Subscription -> updateSubscription(message as StreamMessage<Subscription>)
                }
            }
        }
    }

    private suspend fun updateRoom(message: StreamMessage<Room>) {
        Timber.d("Update Room: ${message.type} - ${message.data.id} - ${message.data.name}")
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

    private suspend fun updateSubscription(message: StreamMessage<Subscription>) {
        Timber.d("Update Subscription: ${message.type} - ${message.data.id} - ${message.data.name}")
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

    private suspend fun reloadRooms() {
        Timber.d("realoadRooms()")
        reloadJob?.cancel()

        try {
            reloadJob = async(CommonPool + strategy.jobs) {
                delay(1000)
                Timber.d("reloading rooms after wait")
                loadRooms()
            }
            reloadJob?.await()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // Update a ChatRoom with a Room information
    private fun updateRoom(room: Room) {
        Timber.d("Updating Room: ${room.id} - ${room.name}")
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
        Timber.d("Updating subscrition: ${subscription.id} - ${subscription.name}")
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
        Timber.d("Removing ROOM: $id")
        synchronized(this) {
            chatRooms.removeAll { chatRoom -> chatRoom.id == id }
        }
        saveChatRoomsInteractor.save(currentServer, sortRooms(chatRooms))
    }

    fun disconnect() {
        manager.removeStatusChannel(stateChannel)
        manager.removeRoomsAndSubscriptionsChannel(subscriptionsChannel)
    }
}