package chat.rocket.android.account.presentation

import chat.rocket.android.server.GetCurrentServerInteractor
import chat.rocket.android.server.RocketChatClientFactory
import chat.rocket.android.util.avatarUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import kotlinx.coroutines.experimental.launch
import timber.log.Timber
import javax.inject.Inject

class AccountPresenter @Inject constructor(
    private val view: AccountView,
    serverInteractor: GetCurrentServerInteractor,
    factory: RocketChatClientFactory
) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)

    fun loadUserProfile() {
        launch {
            view.showLoading()
            try {
                val currentUser = retryIO("me") { client.me() }
                val id = currentUser.id
                val username = currentUser.username
                if (id == null || username == null) {
                    view.showGenericErrorMessage()
                } else {
                    val avatarUrl = serverUrl.avatarUrl(username)
                    view.showProfile(
                        currentUser.name ?: " ",
                        username,
                        avatarUrl,
                        currentUser.status
                    )
                }
            } catch (ex: RocketChatException) {
                Timber.e(ex)
                ex.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }
}