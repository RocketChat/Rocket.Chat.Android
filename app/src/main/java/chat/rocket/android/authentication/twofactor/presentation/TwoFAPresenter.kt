package chat.rocket.android.authentication.twofactor.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.core.internal.rest.login
import javax.inject.Inject

class TwoFAPresenter @Inject constructor(private val view: TwoFAView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator,
                                         private val serverInteractor: GetCurrentServerInteractor,
                                         private val factory: RocketChatClientFactory) {

    // TODO: If the usernameOrEmail and password was informed by the user on the previous screen, then we should pass only the pin, like this: fun authenticate(pin: EditText)
    fun authenticate(usernameOrEmail: String, password: String, twoFactorAuthenticationCode: String) {
        val server = serverInteractor.get()
        if (twoFactorAuthenticationCode.isBlank()) {
            view.alertBlankTwoFactorAuthenticationCode()
        } else if (server == null) {
            navigator.toServerScreen()
        } else {
            launchUI(strategy) {

                val client = factory.create(server)
                if (NetworkHelper.hasInternetAccess()) {
                    view.showLoading()

                    try {
                        // The token is saved via the client TokenProvider
                        client.login(usernameOrEmail, password, twoFactorAuthenticationCode)
                        navigator.toChatList()
                    } catch (exception: RocketChatException) {
                        if (exception is RocketChatAuthException) {
                            view.alertInvalidTwoFactorAuthenticationCode()
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

    fun signup() {
        navigator.toSignUp()
    }
}