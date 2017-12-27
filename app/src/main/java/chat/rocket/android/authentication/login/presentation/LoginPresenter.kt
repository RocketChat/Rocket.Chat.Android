package chat.rocket.android.authentication.login.presentation

import android.widget.EditText
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.util.launchUI
import chat.rocket.android.util.textContent
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatTwoFactorException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.chatRooms
import chat.rocket.core.internal.rest.getRoomFavoriteMessages
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.sendMessage
import timber.log.Timber
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val view: LoginView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator) {
    @Inject lateinit var client: RocketChatClient

    fun authenticate(usernameOrEmailEditText: EditText, passwordEditText: EditText) {
        val usernameOrEmail = usernameOrEmailEditText.textContent
        val password = passwordEditText.textContent

        when {
            usernameOrEmail.isBlank() -> {
                view.shakeView(usernameOrEmailEditText)
            }
            password.isEmpty() -> {
                view.shakeView(passwordEditText)
            }
            else -> {
                launchUI(strategy) {
                    view.showLoading()
                    try {
                        val token = client.login(usernameOrEmail, password)
                        Timber.d("Created token: $token")
                        navigator.toChatList()
                    } catch (rocketChatException: RocketChatException) {
                        when (rocketChatException) {
                            is RocketChatTwoFactorException -> {
                                navigator.toTwoFA(navigator.currentServer!!, usernameOrEmail, password)
                            }
                            else -> {
                                val errorMessage = rocketChatException.message
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
    }

    fun signup() =
            navigator.toSignUp(navigator.currentServer!!)
}