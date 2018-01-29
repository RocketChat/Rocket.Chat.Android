package chat.rocket.android.authentication.twofactor.presentation

import chat.rocket.android.authentication.domain.model.TokenModel
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.MultiServerTokenRepository
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.registerPushToken
import javax.inject.Inject

class TwoFAPresenter @Inject constructor(private val view: TwoFAView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator,
                                         private val multiServerRepository: MultiServerTokenRepository,
                                         private val localRepository: LocalRepository,
                                         private val serverInteractor: GetCurrentServerInteractor,
                                         private val factory: RocketChatClientFactory) {
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)

    // TODO: If the usernameOrEmail and password was informed by the user on the previous screen, then we should pass only the pin, like this: fun authenticate(pin: EditText)
    fun authenticate(usernameOrEmail: String, password: String, twoFactorAuthenticationCode: String) {
        val server = serverInteractor.get()
        when {
            server == null -> {
                navigator.toServerScreen()
            }
            twoFactorAuthenticationCode.isBlank() -> {
                view.alertBlankTwoFactorAuthenticationCode()
            }
            else -> {
                launchUI(strategy) {
                    val client = factory.create(server)
                    if (NetworkHelper.hasInternetAccess()) {
                        view.showLoading()
                        try {
                            // The token is saved via the client TokenProvider
                            val token = client.login(usernameOrEmail, password, twoFactorAuthenticationCode)
                            if (token != null) {
                                multiServerRepository.save(server, TokenModel(token.userId, token.authToken))
                                registerPushToken()
                                navigator.toChatList()
                            } else {
                                view.showGenericErrorMessage()
                            }
                        } catch (exception: RocketChatException) {
                            if (exception is RocketChatAuthException) {
                                view.alertInvalidTwoFactorAuthenticationCode()
                            } else {
                                exception.message?.let {
                                    view.showMessage(it)
                                }.ifNull {
                                    view.showGenericErrorMessage()
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

    fun signup() = navigator.toSignUp()

    private suspend fun registerPushToken() {
        localRepository.get(LocalRepository.KEY_PUSH_TOKEN)?.let {
            client.registerPushToken(it)
        }
        // TODO: Schedule push token registering when it comes up null
    }
}