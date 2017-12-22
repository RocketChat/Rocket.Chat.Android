package chat.rocket.android.authentication.signup.presentation

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
import chat.rocket.core.internal.rest.signup
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject

class SignupPresenter @Inject constructor(private val view: SignupView,
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

    fun signup(nameEditText: EditText, emailEditText: EditText, usernameEditText: EditText, passwordEditText: EditText) {
        val name = nameEditText.textContent
        val email = emailEditText.textContent
        val username = usernameEditText.textContent
        val password = passwordEditText.textContent

        when {
            name.isBlank() -> view.shakeView(nameEditText)
            email.isBlank() -> view.shakeView(emailEditText)
            username.isBlank() -> view.shakeView(usernameEditText)
            password.isEmpty() -> view.shakeView(passwordEditText)
            else -> launchUI(strategy) {
                view.showLoading()

                try {
                    val user = client.signup(email, name, username, password)
                    Timber.d("Created user: $user")

                    val token = client.login(username, password)
                    Timber.d("Logged in: $token")

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
}