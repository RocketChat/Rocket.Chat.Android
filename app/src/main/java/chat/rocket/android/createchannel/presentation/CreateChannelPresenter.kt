package chat.rocket.android.createchannel.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.main.presentation.MainNavigator
import chat.rocket.android.members.uimodel.MemberUiModelMapper
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.RoomType
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.createChannel
import chat.rocket.core.internal.rest.spotlight
import javax.inject.Inject

class CreateChannelPresenter @Inject constructor(
    private val view: CreateChannelView,
    private val strategy: CancelStrategy,
    private val mapper: MemberUiModelMapper,
    private val navigator: MainNavigator,
    val serverInteractor: GetCurrentServerInteractor,
    val factory: RocketChatClientFactory
) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)

    fun createChannel(
        roomType: RoomType,
        channelName: String,
        usersList: List<String>,
        readOnly: Boolean
    ) {
        launchUI(strategy) {
            view.showLoading()
            view.disableUserInput()
            try {
                client.createChannel(roomType, channelName, usersList, readOnly)
                view.prepareToShowChatList()
                view.showChannelCreatedSuccessfullyMessage()
                toChatList()
            } catch (exception: RocketChatException) {
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
                view.enableUserInput()
            }
        }
    }

    fun searchUser(query: String) {
        launchUI(strategy) {
            view.showSuggestionViewInProgress()
            try {
                val users = client.spotlight(query).users
                if (users.isEmpty()) {
                    view.showNoUserSuggestion()
                } else {
                    view.showUserSuggestion(mapper.mapToUiModelList(users))
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

    fun toChatList() = navigator.toChatList()
}