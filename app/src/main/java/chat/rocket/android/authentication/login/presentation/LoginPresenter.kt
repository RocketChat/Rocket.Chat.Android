package chat.rocket.android.authentication.login.presentation

import android.widget.EditText
import chat.rocket.android.authentication.infraestructure.AuthTokenRepository
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.util.launchUI
import chat.rocket.android.util.textContent
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatTwoFactorException
import chat.rocket.common.util.PlatformLogger
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val view: LoginView,
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

    fun authenticate(usernameOrEmail: EditText, password: EditText) {
        val user = usernameOrEmail.textContent
        val pass = password.textContent

        if (user.isBlank() && pass.isEmpty()) {
            view.shakeView(usernameOrEmail)
            view.shakeView(password)
        } else if (user.isBlank()) {
            view.shakeView(usernameOrEmail)
        } else if (pass.isEmpty()) {
            view.shakeView(password)
        } else {
            launchUI(strategy) {
                view.showLoading()
                try {
                    val token = client.login(user, pass)
                    navigator.toChatList()
                } catch (ex: RocketChatException) {
                    when (ex) {
                        is RocketChatTwoFactorException -> navigator.toTwoFA(navigator.currentServer!!, user, pass)
                        else -> {
                            val errorMessage = ex.message
                            if (errorMessage != null) {
                                view.showMessage(errorMessage)
                            }
                        }
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