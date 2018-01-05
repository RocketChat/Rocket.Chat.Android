package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.infrastructure.SharedPreferencesRepository
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatTwoFactorException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.registerPushToken
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
                    if (NetworkHelper.hasInternetAccess()) {
                        view.showLoading()

                        try {
                            client.login(usernameOrEmail, password) // TODO This function returns a user token so should we save it?
                            registerPushToken()
                            navigator.toChatList()
                        } catch (exception: RocketChatException) {
                            if (exception is RocketChatTwoFactorException) {
                                navigator.toTwoFA(usernameOrEmail, password)
                            } else {
                                val message = exception.message
                                if (message != null) {
                                    view.showMessage(message)
                                } else {
                                    view.showGenericErrorMessage()
                                }
                            }
                        }

                        view.hideLoading()
                    } else {
                        view.showNoInternetConnection()
                    }
                }
            }
        }
    }

    fun signup() {
        navigator.toSignUp()
    }

    private suspend fun registerPushToken() {
        // TODO: put it on constructor
        val localRepository: LocalRepository = SharedPreferencesRepository(navigator.activity)

        localRepository.get(LocalRepository.KEY_PUSH_TOKEN)?.let {
            client.registerPushToken(it)
        }

        // TODO: Schedule push token registering when it comes up null
    }
}