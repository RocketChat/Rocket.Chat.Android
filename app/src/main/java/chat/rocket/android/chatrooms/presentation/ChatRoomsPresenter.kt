package chat.rocket.android.chatrooms.presentation

import chat.rocket.android.R
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.ChatRoomEntity
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.server.domain.SettingsRepository
import chat.rocket.android.server.domain.useSpecialCharsOnRoom
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
    private val dbManager: DatabaseManager,
    manager: ConnectionManager,
    private val localRepository: LocalRepository,
    private val userHelper: UserHelper,
    settingsRepository: SettingsRepository
) {
    private val client = manager.client
    private val settings = settingsRepository.get(currentServer)

    fun loadChatRoom(chatRoom: RoomUiModel) {
        launchUI(strategy) {
            view.showLoadingRoom(chatRoom.name)
            try {
                val room = dbManager.getRoom(chatRoom.id)
                if (room != null) {
                    loadChatRoom(room.chatRoom)
                } else {
                    with(chatRoom) {
                        val entity = ChatRoomEntity(
                            id = id,
                            subscriptionId = "",
                            type = type.toString(),
                            name = name.toString(),
                            open = false
                        )
                        loadChatRoom(entity)
                    }
                }
            } finally {
                view.hideLoadingRoom()
            }
        }
    }

    fun loadChatRoom(chatRoom: ChatRoomEntity) {
        with(chatRoom) {
            val isDirectMessage = roomTypeOf(type) is RoomType.DirectMessage
            val roomName = if (settings.useSpecialCharsOnRoom() || (isDirectMessage && settings.useRealName())) {
                    fullname ?: name
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

                    navigator.toChatRoom(
                        chatRoomId =  id,
                        chatRoomName = roomName,
                        chatRoomType = type,
                        isReadOnly = readonly ?: false,
                        chatRoomLastSeen = lastSeen ?: -1,
                        isSubscribed = open,
                        isCreator = ownerId == myself.id || isDirectMessage,
                        isFavorite = favorite ?: false
                    )
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