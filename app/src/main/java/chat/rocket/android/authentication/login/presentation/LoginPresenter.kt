package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetConnectingServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.SaveAccountInteractor
import chat.rocket.android.server.domain.SaveCurrentServerInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.favicon
import chat.rocket.android.server.domain.isLdapAuthenticationEnabled
import chat.rocket.android.server.domain.isPasswordResetEnabled
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.domain.wideTile
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.isEmail
import chat.rocket.android.util.extensions.serverLogoUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatTwoFactorException
import chat.rocket.common.model.Email
import chat.rocket.common.model.Token
import chat.rocket.common.model.User
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.loginWithEmail
import chat.rocket.core.internal.rest.loginWithLdap
import chat.rocket.core.internal.rest.me
import javax.inject.Inject

class LoginPresenter @Inject constructor(
    private val view: LoginView,
    private val strategy: CancelStrategy,
    private val navigator: AuthenticationNavigator,
    private val tokenRepository: TokenRepository,
    private val localRepository: LocalRepository,
    private val settingsInteractor: GetSettingsInteractor,
    private val analyticsManager: AnalyticsManager,
    private val saveCurrentServer: SaveCurrentServerInteractor,
    private val saveAccountInteractor: SaveAccountInteractor,
    private val factory: RocketChatClientFactory,
    val serverInteractor: GetConnectingServerInteractor
) {
    // TODO - we should validate the current server when opening the app, and have a nonnull get()
    private var currentServer = serverInteractor.get()!!
    private lateinit var client: RocketChatClient
    private lateinit var settings: PublicSettings

    fun setupView() {
        setupConnectionInfo(currentServer)
        setupForgotPasswordView()
    }

    private fun setupConnectionInfo(serverUrl: String) {
        currentServer = serverUrl
        client = factory.create(currentServer)
        settings = settingsInteractor.get(currentServer)
    }

    private fun setupForgotPasswordView() {
        if (settings.isPasswordResetEnabled()) {
            view.showForgotPasswordView()
        }
    }

    fun authenticateWithUserAndPassword(usernameOrEmail: String, password: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val token = retryIO("login") {
                    when {
                        settings.isLdapAuthenticationEnabled() ->
                            client.loginWithLdap(usernameOrEmail, password)
                        usernameOrEmail.isEmail() ->
                            client.loginWithEmail(usernameOrEmail, password)
                        else ->
                            client.login(usernameOrEmail, password)
                    }
                }
                val myself = retryIO("me()") { client.me() }
                if (myself.username != null) {
                    val user = User(
                        id = myself.id,
                        roles = myself.roles,
                        status = myself.status,
                        name = myself.name,
                        emails = myself.emails?.map { Email(it.address ?: "", it.verified) },
                        username = myself.username,
                        utcOffset = myself.utcOffset
                    )
                    localRepository.saveCurrentUser(currentServer, user)
                    saveCurrentServer.save(currentServer)
                    localRepository.save(LocalRepository.CURRENT_USERNAME_KEY, myself.username)
                    saveAccount(myself.username!!)
                    saveToken(token)
                    analyticsManager.logLogin(
                        AuthenticationEvent.AuthenticationWithUserAndPassword,
                        true
                    )
                    view.saveSmartLockCredentials(usernameOrEmail, password)
                    navigator.toChatList()
                }
            } catch (exception: RocketChatException) {
                when (exception) {
                    is RocketChatTwoFactorException -> {
                        navigator.toTwoFA(usernameOrEmail, password)
                    }
                    else -> {
                        analyticsManager.logLogin(
                            AuthenticationEvent.AuthenticationWithUserAndPassword,
                            false
                        )
                        exception.message?.let {
                            view.showMessage(it)
                        }.ifNull {
                            view.showGenericErrorMessage()
                        }
                    }
                }
            } finally {
                view.hideLoading()
            }
        }

    }

    fun forgotPassword() = navigator.toForgotPassword()

    private suspend fun saveAccount(username: String) {
        val icon = settings.favicon()?.let {
            currentServer.serverLogoUrl(it)
        }
        val logo = settings.wideTile()?.let {
            currentServer.serverLogoUrl(it)
        }
        val thumb = currentServer.avatarUrl(username)
        val account = Account(currentServer, icon, logo, username, thumb)
        saveAccountInteractor.save(account)
    }

    private fun saveToken(token: Token) = tokenRepository.save(currentServer, token)
}