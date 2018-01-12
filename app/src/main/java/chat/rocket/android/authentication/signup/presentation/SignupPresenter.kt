package chat.rocket.android.authentication.signup.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.signup
import javax.inject.Inject

class SignupPresenter @Inject constructor(private val view: SignupView,
                                          private val strategy: CancelStrategy,
                                          private val navigator: AuthenticationNavigator,
                                          private val serverInteractor: GetCurrentServerInteractor,
                                          private val factory: RocketChatClientFactory) {

    fun signup(name: String, username: String, password: String, email: String) {
        val server = serverInteractor.get()
        when {
            server == null -> {
                navigator.toServerScreen()
            }
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
                val client = factory.create(server)
                launchUI(strategy) {
                    if (NetworkHelper.hasInternetAccess()) {
                        view.showLoading()

                        try {
                            client.signup(email, name, username, password) // TODO This function returns a user so should we save it?
                            client.login(username, password) // TODO This function returns a user token so should we save it?
                            navigator.toChatList()
                        } catch (exception: RocketChatException) {
                            exception.message?.let {
                                view.showMessage(it)
                            }.ifNull {
                                view.showGenericErrorMessage()
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
        serverInteractor.get()?.let {
            navigator.toWebPage(UrlHelper.getTermsOfServiceUrl(it))
        }
    }

    fun privacyPolicy() {
        serverInteractor.get()?.let {
            navigator.toWebPage(UrlHelper.getPrivacyPolicyUrl(it))
        }
    }
}