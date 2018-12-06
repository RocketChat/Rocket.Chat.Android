package chat.rocket.android.userdetails.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.server.domain.GetConnectingServerInteractor
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.internal.rest.createDirectMessage
import chat.rocket.core.internal.rest.spotlight
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
    factory: ConnectionManagerFactory,
    userHelper: UserHelper
) {
    private var currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client
    private val currentUserId = userHelper.user()?.id

    fun loadUserDetails(userId: String) {
        launchUI(strategy) {
            try {
                val user = withContext(CommonPool) {
                    dbManager.userDao().getUser(id = userId)
                }

                user?.let { u ->
                    val localDMs = chatRoomByName(name = u.name)
                    val avatarUrl = u.username?.let { currentServer.avatarUrl(avatar = it) }

                    val chatRoom: ChatRoom? = if (localDMs.isEmpty()) {
                        val query = u.username!!
                        val spotlightResult = retryIO("spotlight($query)") {
                            client.spotlight(query = query)
                        }

                        val matchFromSpotlight = spotlightResult.users.firstOrNull { it.username == query }

                        if (matchFromSpotlight != null) {
                            val result = retryIO("createDirectMessage(${matchFromSpotlight.id}") {
                                client.createDirectMessage(username = matchFromSpotlight.id)
                            }
                            with(matchFromSpotlight) {
                                ChatRoom(
                                    id = result.id,
                                    type = roomTypeOf(RoomType.DIRECT_MESSAGE),
                                    name = u.username ?: u.name.orEmpty(),
                                    fullName = u.name,
                                    favorite = false,
                                    open = false,
                                    alert = false,
                                    status = status,
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
                                    updatedAt = null,
                                    user = null
                                )
                            }
                        } else null
                    } else localDMs.firstOrNull()
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

    fun createDirectMessage(username: String) {

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
