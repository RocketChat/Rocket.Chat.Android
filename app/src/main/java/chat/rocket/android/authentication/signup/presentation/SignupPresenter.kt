package chat.rocket.android.authentication.signup.presentation

import android.widget.EditText
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.util.launchUI
import chat.rocket.android.util.textContent
import chat.rocket.common.RocketChatException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.signup
import timber.log.Timber
import javax.inject.Inject

class SignupPresenter @Inject constructor(private val view: SignupView,
                                          private val strategy: CancelStrategy,
                                          private val navigator: AuthenticationNavigator) {
    @Inject lateinit var client: RocketChatClient

    fun signup(nameEditText: EditText, usernameEditText: EditText, passwordEditText: EditText, emailEditText: EditText) {
        val name = nameEditText.textContent
        val username = usernameEditText.textContent
        val password = passwordEditText.textContent
        val email = emailEditText.textContent

        when {
            name.isBlank() -> {
                view.shakeView(nameEditText)
            }
            username.isBlank() -> {
                view.shakeView(usernameEditText)
            }
            password.isEmpty() -> {
                view.shakeView(passwordEditText)
            }
            email.isBlank() -> {
                view.shakeView(emailEditText)
            }
            else -> {
                launchUI(strategy) {
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

    fun termsOfService() {
        navigator.toTermsOfService()
    }

    fun privacyPolicy() {
        navigator.toPrivacyPolicy()
    }
}