package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.chatroom.viewmodel.ViewModelMapper
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.ChatRoomsSortOrder
import chat.rocket.android.helper.Constants
import chat.rocket.android.helper.SharedPreferenceHelper
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.server.infraestructure.chatRooms
import chat.rocket.android.server.infraestructure.state
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.BaseRoom
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.SimpleUser
import chat.rocket.common.model.User
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.model.Subscription
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.internal.realtime.socket.model.StreamMessage
import chat.rocket.core.internal.realtime.socket.model.Type
import chat.rocket.core.internal.rest.spotlight
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Room
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.channels.Channel
import timber.log.Timber
import javax.inject.Inject
import kotlin.reflect.KProperty1

class ChatRoomsPresenter @Inject constructor(
    private val view: ChatRoomsView,
    private val strategy: CancelStrategy,
    private val navigator: MainNavigator,
    private val serverInteractor: GetCurrentServerInteractor,
    private val getChatRoomsInteractor: GetChatRoomsInteractor,
    private val saveChatRoomsInteractor: SaveChatRoomsInteractor,
    private val saveActiveUsersInteractor: SaveActiveUsersInteractor,
    private val getActiveUsersInteractor: GetActiveUsersInteractor,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val viewModelMapper: ViewModelMapper,
    private val jobSchedulerInteractor: JobSchedulerInteractor,
    settingsRepository: SettingsRepository,
    factory: ConnectionManagerFactory
) {
    private val manager: ConnectionManager = factory.create(serverInteractor.get()!!)
    private val currentServer = serverInteractor.get()!!
    private val client = manager.client
    private var reloadJob: Deferred<List<ChatRoom>>? = null
    private val settings = settingsRepository.get(currentServer)
    private val stateChannel = Channel<State>()
    private val subscriptionsChannel = Channel<StreamMessage<BaseRoom>>()
    private val activeUserChannel = Channel<User>()
    private var lastState = manager.state

    fun loadChatRooms() {
        refreshSettingsInteractor.refreshAsync(currentServer)
        launchUI(strategy) {
            view.showLoading()
            subscribeStatusChange()
            try {
                view.updateChatRooms(getUserChatRooms())
            } catch (ex: RocketChatException) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
                Timber.e(ex)
            } finally {
                view.hideLoading()
            }
            subscribeActiveUsers()
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
                val roomList = getChatRoomsInteractor.getAllByName(currentServer, name)
                if (roomList.isEmpty()) {
                    val (users, rooms) = retryIO("spotlight($name)") {
                        client.spotlight(name)
                    }
                    val chatRoomsCombined = mutableListOf<ChatRoom>()
                    chatRoomsCombined.addAll(usersToChatRooms(users))
                    chatRoomsCombined.addAll(roomsToChatRooms(rooms))
                    val chatRoomsWithPreview = getChatRoomsWithPreviews(chatRoomsCombined)
                    val chatRoomsWithStatus = getChatRoomWithStatus(chatRoomsWithPreview)
                    view.updateChatRooms(chatRoomsWithStatus)
                } else {
                    val chatRoomsWithPreview = getChatRoomsWithPreviews(roomList)
                    val chatRoomsWithStatus = getChatRoomWithStatus(chatRoomsWithPreview)
                    view.updateChatRooms(chatRoomsWithStatus)
                }
            } catch (ex: RocketChatException) {
                Timber.e(ex)
            }
        }
    }

    // In the first time it will not come with the users status, but after called by the
    // [reloadRooms] function may be with.
    private suspend fun getUserChatRooms(): List<ChatRoom> {
        val chatRooms = retryIO("chatRooms") { manager.chatRooms().update }
        val chatRoomsWithPreview = getChatRoomsWithPreviews(chatRooms)
        val chatRoomsWithUserStatus = getChatRoomWithStatus(chatRoomsWithPreview)
        val sortedRooms = sortRooms(chatRoomsWithUserStatus)

        Timber.d("Loaded rooms: ${sortedRooms.size}")
        saveChatRoomsInteractor.save(currentServer, sortedRooms)
        return sortedRooms
    }

    private fun usersToChatRooms(users: List<User>): List<ChatRoom> {
        return users.map {
            ChatRoom(
                id = it.id,
                type = RoomType.DIRECT_MESSAGE,
                user = SimpleUser(username = it.username, name = it.name, id = null),
                status = if (it.name != null) {
                    getActiveUsersInteractor.getActiveUserByUsername(currentServer, it.name!!)
                        ?.status
                } else {
                    null
                },
                name = it.name ?: "",
                fullName = it.name,
                readonly = false,
                updatedAt = null,
                timestamp = null,
                lastSeen = null,
                topic = null,
                description = null,
                announcement = null,
                default = false,
                open = false,
                alert = false,
                unread = 0L,
                userMentions = null,
                groupMentions = 0L,
                lastMessage = null,
                client = client
            )
        }
    }

    private fun roomsToChatRooms(rooms: List<Room>): List<ChatRoom> {
        return rooms.map {
            ChatRoom(
                id = it.id,
                type = it.type,
                user = it.user,
                status = if (it.name != null) {
                    getActiveUsersInteractor.getActiveUserByUsername(currentServer, it.name!!)
                        ?.status
                } else {
                    null
                },
                name = it.name ?: "",
                fullName = it.fullName,
                readonly = it.readonly,
                updatedAt = it.updatedAt,
                timestamp = null,
                lastSeen = null,
                topic = it.topic,
                description = it.description,
                announcement = it.announcement,
                default = false,
                open = false,
                alert = false,
                unread = 0L,
                userMentions = null,
                groupMentions = 0L,
                lastMessage = it.lastMessage,
                client = client
            )
        }
    }

    fun updateSortedChatRooms() {
        launchUI(strategy) {
            val roomList = getChatRoomsInteractor.getAll(currentServer)
            view.updateChatRooms(sortRooms(roomList))
        }
    }

    private fun sortRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        val sortType = SharedPreferenceHelper.getInt(Constants.CHATROOM_SORT_TYPE_KEY, ChatRoomsSortOrder.ACTIVITY)
        val groupByType = SharedPreferenceHelper.getBoolean(Constants.CHATROOM_GROUP_BY_TYPE_KEY, false)

        val openChatRooms = getOpenChatRooms(chatRooms)

        return when (sortType) {
            ChatRoomsSortOrder.ALPHABETICAL -> {
                when (groupByType) {
                    true -> openChatRooms.sortedWith(compareBy(ChatRoom::type).thenBy { it.name })
                    false -> openChatRooms.sortedWith(compareBy(ChatRoom::name))
                }
            }
            ChatRoomsSortOrder.ACTIVITY -> {
                when (groupByType) {
                    true -> openChatRooms.sortedWith(compareBy(ChatRoom::type).thenByDescending { it.lastMessage?.timestamp })
                    false -> openChatRooms.sortedByDescending { chatRoom ->
                        chatRoom.lastMessage?.timestamp
                    }
                }
            }
            else -> {
                openChatRooms
            }
        }
    }

    private fun compareBy(selector: KProperty1<ChatRoom, RoomType>): Comparator<ChatRoom> {
        return Comparator { a, b -> getTypeConstant(a.type) - getTypeConstant(b.type) }
    }

    private fun getTypeConstant(roomType: RoomType): Int {
        return when (roomType) {
            is RoomType.Channel -> Constants.CHATROOM_CHANNEL
            is RoomType.PrivateGroup -> Constants.CHATROOM_PRIVATE_GROUP
            is RoomType.DirectMessage -> Constants.CHATROOM_DM
            is RoomType.Livechat -> Constants.CHATROOM_LIVE_CHAT
            else -> 0
        }
    }

    private fun getChatRoomWithStatus(chatRooms: List<ChatRoom>): List<ChatRoom> {
        val chatRoomsList = mutableListOf<ChatRoom>()
        chatRooms.forEach {
            val newRoom = ChatRoom(
                id = it.id,
                type = it.type,
                user = it.user,
                status = getActiveUsersInteractor.getActiveUserByUsername(
                    currentServer,
                    it.name
                )?.status,
                name = it.name,
                fullName = it.fullName,
                readonly = it.readonly,
                updatedAt = it.updatedAt,
                timestamp = it.timestamp,
                lastSeen = it.lastSeen,
                topic = it.topic,
                description = it.description,
                announcement = it.announcement,
                default = it.default,
                favorite = it.favorite,
                open = it.open,
                alert = it.alert,
                unread = it.unread,
                userMentions = it.userMentions,
                groupMentions = it.groupMentions,
                lastMessage = it.lastMessage,
                client = client
            )
            chatRoomsList.add(newRoom)
        }
        return chatRoomsList
    }

    private suspend fun getChatRoomsWithPreviews(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.map {
            if (it.lastMessage != null) {
                it.copy(lastMessage = viewModelMapper.map(it.lastMessage!!).last().preview)
            } else {
                it
            }
        }
    }

    private fun getOpenChatRooms(chatRooms: List<ChatRoom>): List<ChatRoom> {
        return chatRooms.filter(ChatRoom::open)
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
                        jobSchedulerInteractor.scheduleSendingMessages()
                        reloadRooms()
                        updateChatRooms()
                    }
                }
                lastState = state
            }
        }
    }

    // TODO - Temporary stuff, remove when adding DB support
    private suspend fun subscribeRoomUpdates() {
        manager.addStatusChannel(stateChannel)
        view.showConnectionState(client.state)
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

        updateChatRooms()
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

        updateChatRooms()
    }

    private suspend fun reloadRooms() {
        Timber.d("realoadRooms()")
        reloadJob?.cancel()

        try {
            reloadJob = async(CommonPool + strategy.jobs) {
                delay(1000)
                Timber.d("reloading rooms after wait")
                getUserChatRooms()
            }
            reloadJob?.await()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    // Update a ChatRoom with a Room information
    private fun updateRoom(room: Room) {
        Timber.d("Updating Room: ${room.id} - ${room.name}")
        val chatRooms = getChatRoomsInteractor.getAll(currentServer).toMutableList()
        val chatRoom = chatRooms.find { chatRoom -> chatRoom.id == room.id }
        chatRoom?.apply {
            val newRoom = ChatRoom(
                id = room.id,
                type = room.type,
                user = room.user ?: user,
                status = getActiveUsersInteractor.getActiveUserByUsername(
                    currentServer,
                    room.name ?: name
                )?.status,
                name = room.name ?: name,
                fullName = room.fullName ?: fullName,
                readonly = room.readonly,
                updatedAt = room.updatedAt ?: updatedAt,
                timestamp = timestamp,
                lastSeen = lastSeen,
                topic = room.topic,
                description = room.description,
                announcement = room.announcement,
                default = default,
                favorite = favorite,
                open = open,
                alert = alert,
                unread = unread,
                userMentions = userMentions,
                groupMentions = groupMentions,
                lastMessage = room.lastMessage,
                client = client
            )
            removeRoom(room.id, chatRooms)
            chatRooms.add(newRoom)
            saveChatRoomsInteractor.save(currentServer, sortRooms(chatRooms))
        }
    }

    // Update a ChatRoom with a Subscription information
    private fun updateSubscription(subscription: Subscription) {
        Timber.d("Updating subscription: ${subscription.id} - ${subscription.name}")
        val chatRooms = getChatRoomsInteractor.getAll(currentServer).toMutableList()
        val chatRoom = chatRooms.find { chatRoom -> chatRoom.id == subscription.roomId }
        chatRoom?.apply {
            val newRoom = ChatRoom(
                id = subscription.roomId,
                type = subscription.type,
                user = subscription.user ?: user,
                status = getActiveUsersInteractor.getActiveUserByUsername(
                    currentServer,
                    subscription.name
                )?.status,
                name = subscription.name,
                fullName = subscription.fullName ?: fullName,
                readonly = subscription.readonly ?: readonly,
                updatedAt = subscription.updatedAt ?: updatedAt,
                timestamp = subscription.timestamp ?: timestamp,
                lastSeen = subscription.lastSeen ?: lastSeen,
                topic = topic,
                description = description,
                announcement = announcement,
                default = subscription.isDefault,
                favorite = subscription.isFavorite,
                open = subscription.open,
                alert = subscription.alert,
                unread = subscription.unread,
                userMentions = subscription.userMentions,
                groupMentions = subscription.groupMentions,
                lastMessage = lastMessage,
                client = client
            )
            removeRoom(subscription.roomId, chatRooms)
            chatRooms.add(newRoom)
            saveChatRoomsInteractor.save(currentServer, sortRooms(chatRooms))
        }
    }

    private fun removeRoom(
        id: String,
        chatRooms: MutableList<ChatRoom> = getChatRoomsInteractor.getAll(currentServer).toMutableList()
    ) {
        Timber.d("Removing ROOM: $id")
        synchronized(this) {
            chatRooms.removeAll { chatRoom -> chatRoom.id == id }
        }
        saveChatRoomsInteractor.save(currentServer, sortRooms(chatRooms))
    }

    private suspend fun subscribeActiveUsers() {
        manager.addActiveUserChannel(activeUserChannel)
        launch(CommonPool + strategy.jobs) {
            for (user in activeUserChannel) {
                processActiveUser(user)
            }
        }
    }

    private fun processActiveUser(user: User) {
        // The first activeUsers stream contains all details of the users (username, UTC Offset,
        // etc.), so we add each user to our [saveActiveUsersInteractor] class because the following
        // streams don't contain those details.
        if (!getActiveUsersInteractor.isActiveUserOnRepository(currentServer, user)) {
            Timber.d("Got first active user stream for the user: $user")
            saveActiveUsersInteractor.addActiveUser(currentServer, user)
        } else {
            // After the first stream the next is about the active users updates.
            Timber.d("Got update of active user stream for the user: $user")
            saveActiveUsersInteractor.updateActiveUser(currentServer, user)
        }

        getActiveUsersInteractor.getActiveUserById(currentServer, user.id)?.let {
            updateChatRoomWithUserStatus(it)
        }
    }

    private fun updateChatRoomWithUserStatus(user_: User) {
        Timber.d("active User: $user_")
        val username = user_.username
        val status = user_.status
        if (username != null && status != null) {
            getChatRoomsInteractor.getByName(currentServer, username)?.let {
                val newRoom = ChatRoom(
                    id = it.id,
                    type = it.type,
                    user = it.user,
                    status = status,
                    name = it.name,
                    fullName = it.fullName,
                    readonly = it.readonly,
                    updatedAt = it.updatedAt,
                    timestamp = it.timestamp,
                    lastSeen = it.lastSeen,
                    topic = it.topic,
                    description = it.description,
                    announcement = it.announcement,
                    default = it.default,
                    favorite = it.favorite,
                    open = it.open,
                    alert = it.alert,
                    unread = it.unread,
                    userMentions = it.userMentions,
                    groupMentions = it.groupMentions,
                    lastMessage = it.lastMessage,
                    client = client
                )

                getChatRoomsInteractor.remove(currentServer, it)
                getChatRoomsInteractor.add(currentServer, newRoom)
                launchUI(strategy) {
                    view.updateChatRooms(sortRooms(getChatRoomsInteractor.getAll(currentServer)))
                }
            }
        }
    }

    private fun updateChatRooms() {
        Timber.i("Updating ChatRooms")
        launch(strategy.jobs) {
            val chatRoomsWithPreview = getChatRoomsWithPreviews(
                getChatRoomsInteractor.getAll(currentServer)
            )
            val chatRoomsWithStatus = getChatRoomWithStatus(chatRoomsWithPreview)
            view.updateChatRooms(chatRoomsWithStatus)
        }
    }

    fun disconnect() {
        manager.removeStatusChannel(stateChannel)
        manager.removeRoomsAndSubscriptionsChannel(subscriptionsChannel)
        manager.removeActiveUserChannel(activeUserChannel)
    }
}