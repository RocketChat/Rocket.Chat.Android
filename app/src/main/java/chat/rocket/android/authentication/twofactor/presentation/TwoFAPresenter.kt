package chat.rocket.android.authentication.twofactor.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.android.util.extensions.serverLogoUrl
import chat.rocket.android.util.helper.analytics.AnalyticsManager
import chat.rocket.android.util.helper.analytics.event.AuthenticationEvent
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.me
import chat.rocket.core.model.Myself
import javax.inject.Inject

class TwoFAPresenter @Inject constructor(
    private val view: TwoFAView,
    private val strategy: CancelStrategy,
    private val navigator: AuthenticationNavigator,
    private val tokenRepository: TokenRepository,
    private val localRepository: LocalRepository,
    private val serverInteractor: GetConnectingServerInteractor,
    private val saveCurrentServerInteractor: SaveCurrentServerInteractor,
    private val analyticsTrackingInteractor: AnalyticsTrackingInteractor,
    private val factory: RocketChatClientFactory,
    private val saveAccountInteractor: SaveAccountInteractor,
    private val getAccountsInteractor: GetAccountsInteractor,
    settingsInteractor: GetSettingsInteractor
) {
    private val currentServer = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(currentServer)
    private var settings: PublicSettings = settingsInteractor.get(serverInteractor.get()!!)

    // TODO: If the usernameOrEmail and password was informed by the user on the previous screen, then we should pass only the pin, like this: fun authenticate(pin: EditText)
    fun authenticate(
        usernameOrEmail: String,
        password: String,
        twoFactorAuthenticationCode: String
    ) {
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
                    view.showLoading()
                    try {
                        // The token is saved via the client TokenProvider
                        val token = retryIO("login") {
                            client.login(usernameOrEmail, password, twoFactorAuthenticationCode)
                        }
                        val me = retryIO("me") { client.me() }
                        saveAccount(me)
                        saveCurrentServerInteractor.save(currentServer)
                        tokenRepository.save(server, token)
                        registerPushToken()
                        if (analyticsTrackingInteractor.get()) {
                            AnalyticsManager.logLogin(
                                AuthenticationEvent.AuthenticationWithUserAndPassword,
                                true
                            )
                        }
                        navigator.toChatList()
                    } catch (exception: RocketChatException) {
                        if (exception is RocketChatAuthException) {
                            view.alertInvalidTwoFactorAuthenticationCode()
                        } else {
                            if (analyticsTrackingInteractor.get()) {
                                AnalyticsManager.logLogin(
                                    AuthenticationEvent.AuthenticationWithUserAndPassword,
                                    false
                                )
                            }
                            exception.message?.let {
                                view.showMessage(it)
                            }.ifNull {
                                view.showGenericErrorMessage()
                            }
                        }
                    } finally {
                        view.hideLoading()
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
            currentServer.serverLogoUrl(it)
        }
        val logo = settings.wideTile()?.let {
            currentServer.serverLogoUrl(it)
        }
        val thumb = currentServer.avatarUrl(me.username!!)
        val account = Account(currentServer, icon, logo, me.username!!, thumb)
        saveAccountInteractor.save(account)
    }
}