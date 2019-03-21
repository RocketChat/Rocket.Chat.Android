package chat.rocket.android.authentication.loginoptions.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
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
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.domain.wideTile
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.serverLogoUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.Email
import chat.rocket.common.model.Token
import chat.rocket.common.model.User
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.loginWithCas
import chat.rocket.core.internal.rest.loginWithOauth
import chat.rocket.core.internal.rest.loginWithSaml
import chat.rocket.core.internal.rest.me
import kotlinx.coroutines.delay
import javax.inject.Inject

private const val TYPE_LOGIN_OAUTH = 1
private const val TYPE_LOGIN_CAS = 2
private const val TYPE_LOGIN_SAML = 3
private const val TYPE_LOGIN_DEEP_LINK = 4

class LoginOptionsPresenter @Inject constructor(
    private val view: LoginOptionsView,
    private val strategy: CancelStrategy,
    private val factory: RocketChatClientFactory,
    private val navigator: AuthenticationNavigator,
    private val settingsInteractor: GetSettingsInteractor,
    private val localRepository: LocalRepository,
    private val saveCurrentServer: SaveCurrentServerInteractor,
    private val saveAccountInteractor: SaveAccountInteractor,
    private val analyticsManager: AnalyticsManager,
    private val tokenRepository: TokenRepository,
    serverInteractor: GetConnectingServerInteractor
) {
    // TODO - we should validate the current server when opening the app, and have a nonnull get()
    private var currentServer = serverInteractor.get()!!
    private lateinit var client: RocketChatClient
    private lateinit var settings: PublicSettings
    private lateinit var credentialToken: String
    private lateinit var credentialSecret: String
    private lateinit var deepLinkUserId: String
    private lateinit var deepLinkToken: String
    private lateinit var loginMethod: AuthenticationEvent

    fun toCreateAccount() = navigator.toCreateAccount()

    fun toLoginWithEmail() = navigator.toLogin(currentServer)

    fun authenticateWithOauth(oauthToken: String, oauthSecret: String) {
        setupConnectionInfo(currentServer)
        credentialToken = oauthToken
        credentialSecret = oauthSecret
        loginMethod = AuthenticationEvent.AuthenticationWithOauth
        doAuthentication(TYPE_LOGIN_OAUTH)
    }

    fun authenticateWithCas(casToken: String) {
        setupConnectionInfo(currentServer)
        credentialToken = casToken
        loginMethod = AuthenticationEvent.AuthenticationWithCas
        doAuthentication(TYPE_LOGIN_CAS)
    }

    fun authenticateWithSaml(samlToken: String) {
        setupConnectionInfo(currentServer)
        credentialToken = samlToken
        loginMethod = AuthenticationEvent.AuthenticationWithSaml
        doAuthentication(TYPE_LOGIN_SAML)
    }

    fun authenticateWithDeepLink(deepLinkInfo: LoginDeepLinkInfo) {
        val serverUrl = deepLinkInfo.url
        setupConnectionInfo(serverUrl)
        if (deepLinkInfo.userId != null && deepLinkInfo.token != null) {
            deepLinkUserId = deepLinkInfo.userId
            deepLinkToken = deepLinkInfo.token
            tokenRepository.save(serverUrl, Token(deepLinkUserId, deepLinkToken))
            loginMethod = AuthenticationEvent.AuthenticationWithDeeplink
            doAuthentication(TYPE_LOGIN_DEEP_LINK)
        }
    }

    private fun doAuthentication(loginType: Int) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val token = retryIO("login") {
                    when (loginType) {
                        TYPE_LOGIN_OAUTH -> client.loginWithOauth(credentialToken, credentialSecret)
                        TYPE_LOGIN_CAS -> {
                            delay(3000)
                            client.loginWithCas(credentialToken)
                        }
                        TYPE_LOGIN_SAML -> {
                            delay(3000)
                            client.loginWithSaml(credentialToken)
                        }
                        TYPE_LOGIN_DEEP_LINK -> {
                            val myself = client.me() // Just checking if the credentials worked.
                            if (myself.id == deepLinkUserId) {
                                Token(deepLinkUserId, deepLinkToken)
                            } else {
                                throw RocketChatAuthException("Invalid AuthenticationEvent Deep Link Credentials...")
                            }
                        }
                        else -> {
                            throw IllegalStateException(
                                "Expected TYPE_LOGIN_USER_EMAIL, " +
                                        "TYPE_LOGIN_CAS,TYPE_LOGIN_SAML, TYPE_LOGIN_OAUTH or " +
                                        "TYPE_LOGIN_DEEP_LINK"
                            )
                        }
                    }
                }
                val myself = retryIO("me()") { client.me() }
                myself.username?.let { username ->
                    val user = User(
                        id = myself.id,
                        roles = myself.roles,
                        status = myself.status,
                        name = myself.name,
                        emails = myself.emails?.map { Email(it.address ?: "", it.verified) },
                        username = myself.username,
                        utcOffset = myself.utcOffset
                    )
                    localRepository.saveCurrentUser(url = currentServer, user = user)
                    saveCurrentServer.save(currentServer)
                    saveAccount(username)
                    saveToken(token)
                    analyticsManager.logLogin(loginMethod, true)
                    navigator.toChatList()
                }.ifNull {
                    if (loginType == TYPE_LOGIN_OAUTH) {
                        navigator.toRegisterUsername(token.userId, token.authToken)
                    }
                }
            } catch (exception: RocketChatException) {
                analyticsManager.logLogin(loginMethod, false)
                exception.message?.let {
                    view.showMessage(it)
                }.ifNull {
                    view.showGenericErrorMessage()
                }
            } finally {
                view.hideLoading()
            }
        }
    }

    private fun setupConnectionInfo(serverUrl: String) {
        currentServer = serverUrl
        client = factory.create(currentServer)
        settings = settingsInteractor.get(currentServer)
    }

    private fun saveAccount(username: String) {
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