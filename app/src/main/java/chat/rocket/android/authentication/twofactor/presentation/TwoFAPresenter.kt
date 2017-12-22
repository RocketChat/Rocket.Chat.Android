package chat.rocket.android.authentication.twofactor.presentation

import android.widget.EditText
import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.util.launchUI
import chat.rocket.android.util.textContent
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
    // TODO: Create a single entry point to RocketChatClient
    val client: RocketChatClient = RocketChatClient.create {
        httpClient = okHttpClient
        restUrl = HttpUrl.parse(navigator.currentServer)!!
        websocketUrl = navigator.currentServer!!
        tokenRepository = repository
        platformLogger = logger
    }

    // TODO: If the usernameOrEmail and password was informed by the user on the previous screen, then we should pass only the pin, like this: fun authenticate(pin: EditText)
    fun authenticate(usernameOrEmail: String, password: String, pin: EditText) {
       val twoFACode = pin.textContent
        if (twoFACode.isBlank()) {
            view.shakeView(pin)
        } else {
            launchUI(strategy) {
                view.showLoading()
                try {
                    val token = client.login(usernameOrEmail, password, twoFACode)
                    // Todo Salve token.
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
    }

    fun signup() {
        navigator.toSignUp(navigator.currentServer!!)
    }
}