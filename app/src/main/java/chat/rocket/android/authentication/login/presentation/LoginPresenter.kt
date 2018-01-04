package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatTwoFactorException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import timber.log.Timber
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val view: LoginView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator) {
    @Inject lateinit var client: RocketChatClient

    fun authenticate(usernameOrEmail: String, password: String) {
        when {
            usernameOrEmail.isBlank() -> {
                view.alertWrongUsernameOrEmail()
            }
            password.isEmpty() -> {
                view.alertWrongPassword()
            }
            else -> {
                launchUI(strategy) {
                    view.showLoading()
                    if (NetworkHelper.hasInternetAccess()) {
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
                    } else {
                        view.showNoInternetConnection()
                    }
                }
            }
        }
    }

    fun signup() {
        navigator.toSignUp(navigator.currentServer!!)
    }
}