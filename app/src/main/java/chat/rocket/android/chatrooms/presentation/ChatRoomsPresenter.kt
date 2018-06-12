package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.R
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.infraestructure.ConnectionManager
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.model.User
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.internal.realtime.createDirectMessage
import chat.rocket.core.internal.rest.me
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class ChatRoomsPresenter @Inject constructor(
    private val view: ChatRoomsView,
    private val strategy: CancelStrategy,
    private val navigator: MainNavigator,
    @Named("currentServer") private val currentServer: String,
    manager: ConnectionManager,
    private val localRepository: LocalRepository,
    private val userHelper: UserHelper,
    settingsRepository: SettingsRepository
) {
    private val client = manager.client
    private val settings = settingsRepository.get(currentServer)

    fun loadChatRoom(chatRoom: chat.rocket.android.db.model.ChatRoom) {
        with(chatRoom.chatRoom) {
            val isDirectMessage = roomTypeOf(type) is RoomType.DirectMessage
            val roomName = if (isDirectMessage
                    && fullname != null
                    && settings.useRealName()) {
                fullname!!
            } else {
                name
            }

            launchUI(strategy) {
                val myself = getCurrentUser()
                if (myself?.username == null) {
                    view.showMessage(R.string.msg_generic_error)
                } else {
                    val id = if (isDirectMessage && !open) {
                        retryIO("createDirectMessage($name)") {
                            client.createDirectMessage(name)
                        }
                        val fromTo = mutableListOf(myself.id, id).apply {
                            sort()
                        }
                        fromTo.joinToString("")
                    } else {
                        id
                    }
                    val isChatRoomOwner = ownerId == myself.id || isDirectMessage
                    navigator.toChatRoom(id, roomName, type, readonly ?: false,
                            lastSeen ?: -1, open, isChatRoomOwner)
                }
            }
        }
    }

    private suspend fun getCurrentUser(): User? {
        userHelper.user()?.let {
            return it
        }
        try {
            val myself = retryIO { client.me() }
            val user = User(
                id = myself.id,
                username = myself.username,
                name = myself.name,
                status = myself.status,
                utcOffset = myself.utcOffset,
                emails = null,
                roles = myself.roles
            )
            localRepository.saveCurrentUser(url = currentServer, user = user)
        } catch (ex: RocketChatException) {
            Timber.e(ex)
        }
        return null
    }
}