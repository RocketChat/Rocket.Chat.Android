package chat.rocket.android.authentication.signup.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.util.launchUI
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

    fun signup(name: String, username: String, password: String, email: String) {
        when {
            name.isBlank() -> {
                view.alertBlankName()
            }
            username.isBlank() -> {
                view.alertBlankUsername()
            }
            password.isEmpty() -> {
                view.alertEmptyPassword()
            }
            email.isBlank() -> {
                view.alertBlankEmail()
            }
            else -> {
                launchUI(strategy) {
                    if (NetworkHelper.hasInternetAccess()) {
                        view.showLoading()
                        try {
                            val user = client.signup(email, name, username, password)
                            Timber.d("Created user: $user")

                            val token = client.login(username, password)
                            Timber.d("Logged in. Token: $token")

                            navigator.toChatList()
                        } catch (ex: RocketChatException) {
                            val errorMessage = ex.message
                            if (errorMessage != null) {
                                view.showMessage(errorMessage)
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

    fun termsOfService() {
        navigator.toTermsOfService()
    }

    fun privacyPolicy() {
        navigator.toPrivacyPolicy()
    }
}