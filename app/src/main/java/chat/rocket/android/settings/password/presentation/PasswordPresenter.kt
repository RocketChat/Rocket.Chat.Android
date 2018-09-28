package chat.rocket.android.settings.password.presentation

import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.updateProfile
import javax.inject.Inject

class PasswordPresenter @Inject constructor(
    private val view: PasswordView,
    private val strategy: CancelStrategy,
    serverInteractor: GetCurrentServerInteractor,
    factory: RocketChatClientFactory
) {
    private val serverUrl = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(serverUrl)

    fun updatePassword(password: String) {
        launchUI(strategy) {
            try {
                view.showLoading()

                val me = retryIO("me") { client.me() }
                retryIO("updateProfile(${me.id})") {
                    client.updateProfile(me.id, null, null, password, null)
                }

                view.showPasswordSuccessfullyUpdatedMessage()
                view.hideLoading()
            } catch (exception: RocketChatException) {
                view.showPasswordFailsUpdateMessage(exception.message)
                view.hideLoading()
            }
        }
    }
}