package chat.rocket.android.authentication.twofactor.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.me
import chat.rocket.core.model.Myself
import javax.inject.Inject

class TwoFAPresenter @Inject constructor(private val view: TwoFAView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator,
                                         private val tokenRepository: TokenRepository,
                                         private val localRepository: LocalRepository,
                                         private val serverInteractor: GetCurrentServerInteractor,
                                         private val factory: RocketChatClientFactory,
                                         private val saveAccountInteractor: SaveAccountInteractor,
                                         private val getAccountsInteractor: GetAccountsInteractor,
                                         settingsInteractor: GetSettingsInteractor) {
    private val currentServer = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(currentServer)
    private var settings: PublicSettings = settingsInteractor.get(serverInteractor.get()!!)

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
                            val token =
                                client.login(usernameOrEmail, password, twoFactorAuthenticationCode)
                            val me = client.me()
                            saveAccount(me)
                            tokenRepository.save(server, token)
                            registerPushToken()
                            navigator.toChatList()
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
            client.registerPushToken(it, getAccountsInteractor.get(), factory)
        }
        // TODO: When the push token is null, at some point we should receive it with
        // onTokenRefresh() on FirebaseTokenService, we need to confirm it.
    }

    private suspend fun saveAccount(me: Myself) {
        val icon = settings.favicon()?.let {
            UrlHelper.getServerLogoUrl(currentServer, it)
        }
        val logo = settings.wideTile()?.let {
            UrlHelper.getServerLogoUrl(currentServer, it)
        }
        val thumb = UrlHelper.getAvatarUrl(currentServer, me.username!!)
        val account = Account(currentServer, icon, logo, me.username!!, thumb)
        saveAccountInteractor.save(account)
    }
}