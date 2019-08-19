package chat.rocket.android.members.presentation

import chat.rocket.android.chatroom.presentation.ChatRoomNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.members.uimodel.MemberUiModelMapper
import chat.rocket.android.server.domain.ADD_USER_TO_JOINED_ROOM
import chat.rocket.android.server.domain.PermissionsInteractor
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.getMembers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class MembersPresenter @Inject constructor(
    private val view: MembersView,
    private val navigator: ChatRoomNavigator,
    private val dbManager: DatabaseManager,
    private val permissionsInteractor: PermissionsInteractor,
    @Named("currentServer") private val currentServer: String?,
    private val strategy: CancelStrategy,
    private val mapper: MemberUiModelMapper,
    val factory: RocketChatClientFactory,
    private val userHelper: UserHelper
) {
    private val client: RocketChatClient? = currentServer?.let { factory.get(it) }
    var offset: Long = 0

    /**
     * Loads all the chat room members for the given room id.
     *
     * @param roomId The id of the room to get chat room members from.
     */
    fun loadChatRoomsMembers(roomId: String) {
        launchUI(strategy) {
            try {
                view.showLoading()
                dbManager.getRoom(roomId)?.let {
                    client?.getMembers(roomId, roomTypeOf(it.chatRoom.type), offset, 30)
                        ?.let { members ->
                            val memberUiModels = mapper.mapToUiModelList(members.result)
                            view.showMembers(memberUiModels, members.total)
                            offset += 1 * 30L
                        }
                }.ifNull {
                    Timber.e("Couldn't find a room with id: $roomId at current server.")
                }
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun checkInviteUserPermission(chatRoomId: String) {
        launchUI(strategy) {
            if (hasInviteUserPermission(chatRoomId)) {
                view.showInviteUsersButton()
            } else {
                view.hideInviteUserButton()
            }
        }
    }

    private suspend fun hasInviteUserPermission(chatRoomId: String): Boolean {
        return permissionsInteractor.hasPermission(ADD_USER_TO_JOINED_ROOM, chatRoomId)
    }

    fun toMemberDetails(memberUiModel: MemberUiModel, chatRoomId: String) {
        with(memberUiModel) {
            if (userId != userHelper.user()?.id) {
                navigator.toMemberDetails(userId, chatRoomId)
            }
        }
    }

    fun toInviteUsers(chatRoomId: String) {
        navigator.toInviteUsers(chatRoomId)
    }
}
