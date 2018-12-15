package chat.rocket.android.userdetails.presentation

import chat.rocket.android.chatrooms.domain.FetchChatRoomsInteractor
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.server.domain.GetConnectingServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.model.userStatusOf
import chat.rocket.core.internal.rest.createDirectMessage
import chat.rocket.core.model.ChatRoom
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import timber.log.Timber
import javax.inject.Inject

class UserDetailsPresenter @Inject constructor(
    private val view: UserDetailsView,
    private val dbManager: DatabaseManager,
    private val strategy: CancelStrategy,
    serverInteractor: GetConnectingServerInteractor,
    factory: ConnectionManagerFactory
) {
    private var currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client
    private val interactor = FetchChatRoomsInteractor(client,dbManager)

    fun loadUserDetails(userId: String) {
        launchUI(strategy) {
            try {
                val user = withContext(CommonPool) {
                    dbManager.userDao().getUser(id = userId)
                }

                user?.let { u ->
                    val openedChatRooms = chatRoomByName(name = u.name)
                    val avatarUrl = u.username?.let { currentServer.avatarUrl(avatar = it) }

                    val chatRoom: ChatRoom? = openedChatRooms.firstOrNull()

                    view.showUserDetails(
                        avatarUrl = avatarUrl,
                        username = u.username,
                        name = u.name,
                        utcOffset = u.utcOffset,
                        status = u.status,
                        chatRoom = chatRoom
                    )
                }
            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }
    }

    fun createDirectMessage(id: String) = launchUI(strategy) {
        try {
            val result = retryIO("createDirectMessage($id") {
                client.createDirectMessage(username = id)
            }

            interactor.refreshChatRooms()

            val userEntity = withContext(CommonPool) {
                dbManager.userDao().getUser(id = id)
            }

            if (userEntity != null) {
                view.toDirectMessage(
                    chatRoom = ChatRoom(
                        id = result.id,
                        type = roomTypeOf(RoomType.DIRECT_MESSAGE),
                        name = userEntity.username ?: userEntity.name.orEmpty(),
                        fullName = userEntity.name,
                        favorite = false,
                        open = false,
                        alert = false,
                        status = userStatusOf(userEntity.status),
                        client = client,
                        broadcast = false,
                        archived = false,
                        default = false,
                        description = null,
                        groupMentions = null,
                        userMentions = null,
                        lastMessage = null,
                        lastSeen = null,
                        topic = null,
                        announcement = null,
                        roles = null,
                        unread = 0,
                        readonly = false,
                        muted = null,
                        subscriptionId = "",
                        timestamp = null,
                        updatedAt = result.updatedAt,
                        user = null
                    )
                )
            }
        } catch (ex: Exception) {
            Timber.e(ex)
            view.onOpenDirectMessageError()
        }
    }
    private suspend fun chatRoomByName(name: String? = null): List<ChatRoom> = withContext(CommonPool) {
        return@withContext dbManager.chatRoomDao().getAllSync().filter {
            if (name == null) {
                return@filter true
            }
            it.chatRoom.name == name || it.chatRoom.fullname == name
        }.map {
            with(it.chatRoom) {
                ChatRoom(
                    id = id,
                    subscriptionId = subscriptionId,
                    type = roomTypeOf(type),
                    unread = unread,
                    broadcast = broadcast ?: false,
                    alert = alert,
                    fullName = fullname,
                    name = name ?: "",
                    favorite = favorite ?: false,
                    default = isDefault ?: false,
                    readonly = readonly,
                    open = open,
                    lastMessage = null,
                    archived = false,
                    status = null,
                    user = null,
                    userMentions = userMentions,
                    client = client,
                    announcement = null,
                    description = null,
                    groupMentions = groupMentions,
                    roles = null,
                    topic = null,
                    lastSeen = this.lastSeen,
                    timestamp = timestamp,
                    updatedAt = updatedAt
                )
            }
        }
    }
}
