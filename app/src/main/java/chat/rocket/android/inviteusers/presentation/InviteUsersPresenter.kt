package chat.rocket.android.inviteusers.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.members.uimodel.MemberUiModel
import chat.rocket.android.members.uimodel.MemberUiModelMapper
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.roomTypeOf
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.invite
import chat.rocket.core.internal.rest.spotlight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Named

class InviteUsersPresenter @Inject constructor(
    private val view: InviteUsersView,
    private val dbManager: DatabaseManager,
    @Named("currentServer") private val currentServer: String?,
    private val strategy: CancelStrategy,
    private val mapper: MemberUiModelMapper,
    val factory: RocketChatClientFactory
) {
    private val client: RocketChatClient? = currentServer?.let { factory.get(it) }

    fun inviteUsers(chatRoomId: String, usersList: List<MemberUiModel>) {
        launchUI(strategy) {
            view.disableUserInput()
            view.showLoading()

            try {
                usersList.forEach { user ->
                    try {
                        client?.invite(
                            chatRoomId,
                            roomTypeOf(getChatRoomType(chatRoomId)),
                            user.userId
                        )
                    } catch (exception: RocketChatException) {
                        exception.message?.let {
                            view.showMessage(it)
                        }.ifNull {
                            view.showGenericErrorMessage()
                        }
                    }
                }
            } finally {
                view.hideLoading()
                view.enableUserInput()
                view.usersInvitedSuccessfully()
            }
        }
    }

    fun searchUser(query: String) {
        launchUI(strategy) {
            view.showSuggestionViewInProgress()
            try {
                client?.spotlight(query)?.users?.let { users ->
                    if (users.isEmpty()) {
                        view.showNoUserSuggestion()
                    } else {
                        view.showUserSuggestion(mapper.mapToUiModelList(users))
                    }
                }
            } catch (ex: RocketChatException) {
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideSuggestionViewInProgress()
            }
        }
    }

    private suspend fun getChatRoomType(chatRoomId: String): String {
        return withContext(Dispatchers.IO + strategy.jobs) {
            return@withContext dbManager.getRoom(chatRoomId)?.chatRoom.let { it?.type ?: "" }
        }
    }
}