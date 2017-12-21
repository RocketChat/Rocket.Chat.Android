package chat.rocket.android.authentication.twofactor.presentation

import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import javax.inject.Inject

class TwoFAPresenter @Inject constructor(private val view: TwoFAView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator,
                                         private val okHttpClient: OkHttpClient,
                                         private val logger: PlatformLogger,
                                         private val repository: AuthTokenRepository) {

    val client: RocketChatClient = RocketChatClient.create {
        httpClient = okHttpClient
        restUrl = HttpUrl.parse(navigator.currentServer)!!
        websocketUrl = navigator.currentServer!!
        tokenRepository = repository
        platformLogger = logger
    }

    fun authenticate(username: String, password: String, pin: String) {
        // TODO - validate input

        launchUI(strategy) {
            view.showLoading()
            try {
                val token = client.login(username, password, pin)

                navigator.toChatList()
            } catch (ex: RocketChatException) {
                val errorMessage = ex.message
                if (errorMessage != null) {
                    view.showMessage(errorMessage)
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    fun signup() {
        navigator.toSignUp(navigator.currentServer!!)
    }
}