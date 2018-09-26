package chat.rocket.android.authentication.loginoptions.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.OauthHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetConnectingServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.SaveAccountInteractor
import chat.rocket.android.server.domain.SaveCurrentServerInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.casLoginUrl
import chat.rocket.android.server.domain.favicon
import chat.rocket.android.server.domain.gitlabUrl
import chat.rocket.android.server.domain.isCasAuthenticationEnabled
import chat.rocket.android.server.domain.isFacebookAuthenticationEnabled
import chat.rocket.android.server.domain.isGithubAuthenticationEnabled
import chat.rocket.android.server.domain.isGitlabAuthenticationEnabled
import chat.rocket.android.server.domain.isGoogleAuthenticationEnabled
import chat.rocket.android.server.domain.isLinkedinAuthenticationEnabled
import chat.rocket.android.server.domain.isLoginFormEnabled
import chat.rocket.android.server.domain.isRegistrationEnabledForNewUsers
import chat.rocket.android.server.domain.isWordpressAuthenticationEnabled
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.domain.wideTile
import chat.rocket.android.server.domain.wordpressUrl
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.casUrl
import chat.rocket.android.util.extensions.generateRandomString
import chat.rocket.android.util.extensions.parseColor
import chat.rocket.android.util.extensions.samlUrl
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
import chat.rocket.core.internal.rest.settingsOauth
import kotlinx.coroutines.experimental.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TYPE_LOGIN_OAUTH = 1
private const val TYPE_LOGIN_CAS = 2
private const val TYPE_LOGIN_SAML = 3
private const val TYPE_LOGIN_DEEP_LINK = 4
private const val SERVICE_NAME_FACEBOOK = "facebook"
private const val SERVICE_NAME_GITHUB = "github"
private const val SERVICE_NAME_GOOGLE = "google"
private const val SERVICE_NAME_LINKEDIN = "linkedin"
private const val SERVICE_NAME_GILAB = "gitlab"
private const val SERVICE_NAME_WORDPRESS = "wordpress"

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

    fun setupView() {
        setupConnectionInfo(currentServer)
        setupAccountContainerView()
        setupLoginWithEmailView()
        setupCreateNewAccountView()
    }

    private fun setupConnectionInfo(serverUrl: String) {
        currentServer = serverUrl
        client = factory.create(currentServer)
        settings = settingsInteractor.get(currentServer)
    }

    private fun setupAccountContainerView() {
        launchUI(strategy) {
            try {
                val services = retryIO("settingsOauth()") {
                    client.settingsOauth().services
                }

                if (services.isNotEmpty()) {
                    val state = OauthHelper.getState()
                    var totalSocialAccountsEnabled = 0

                    // OAuth accounts.
                    if (settings.isFacebookAuthenticationEnabled()) {
                        getServiceMap(services, SERVICE_NAME_FACEBOOK)?.let { serviceMap ->
                            getOauthClientId(serviceMap)?.let { clientId ->
                                view.setupFacebookButtonListener(
                                    OauthHelper.getFacebookOauthUrl(
                                        clientId,
                                        currentServer,
                                        state
                                    ), state
                                )
                                view.enableLoginByFacebook()
                                totalSocialAccountsEnabled++
                            }
                        }
                    }

                    if (settings.isGithubAuthenticationEnabled()) {
                        getServiceMap(services, SERVICE_NAME_GITHUB)?.let { serviceMap ->
                            getOauthClientId(serviceMap)?.let { clientId ->
                                view.setupGithubButtonListener(
                                    OauthHelper.getGithubOauthUrl(
                                        clientId,
                                        state
                                    ), state
                                )
                                view.enableLoginByGithub()
                                totalSocialAccountsEnabled++
                            }
                        }
                    }

                    if (settings.isGoogleAuthenticationEnabled()) {
                        getServiceMap(services, SERVICE_NAME_GOOGLE)?.let { serviceMap ->
                            getOauthClientId(serviceMap)?.let { clientId ->
                                view.setupGoogleButtonListener(
                                    OauthHelper.getGoogleOauthUrl(
                                        clientId,
                                        currentServer,
                                        state
                                    ), state
                                )
                                view.enableLoginByGoogle()
                                totalSocialAccountsEnabled++
                            }
                        }
                    }

                    if (settings.isLinkedinAuthenticationEnabled()) {
                        getServiceMap(services, SERVICE_NAME_LINKEDIN)?.let { serviceMap ->
                            getOauthClientId(serviceMap)?.let { clientId ->
                                view.setupLinkedinButtonListener(
                                    OauthHelper.getLinkedinOauthUrl(
                                        clientId,
                                        currentServer,
                                        state
                                    ), state
                                )
                                view.enableLoginByLinkedin()
                                totalSocialAccountsEnabled++

                            }
                        }
                    }

                    if (settings.isGitlabAuthenticationEnabled()) {
                        getServiceMap(services, SERVICE_NAME_GILAB)?.let { serviceMap ->
                            getOauthClientId(serviceMap)?.let { clientId ->
                                val gitlabOauthUrl = if (settings.gitlabUrl() != null) {
                                    OauthHelper.getGitlabOauthUrl(
                                        host = settings.gitlabUrl(),
                                        clientId = clientId,
                                        serverUrl = currentServer,
                                        state = state
                                    )
                                } else {
                                    OauthHelper.getGitlabOauthUrl(
                                        clientId = clientId,
                                        serverUrl = currentServer,
                                        state = state
                                    )
                                }
                                view.setupGitlabButtonListener(gitlabOauthUrl, state)
                                view.enableLoginByGitlab()
                                totalSocialAccountsEnabled++
                            }
                        }
                    }

                    if (settings.isWordpressAuthenticationEnabled()) {
                        getServiceMap(services, SERVICE_NAME_WORDPRESS)?.let { serviceMap ->
                            getOauthClientId(serviceMap)?.let { clientId ->
                                val wordpressOauthUrl =
                                    if (settings.wordpressUrl().isNullOrEmpty()) {
                                        OauthHelper.getWordpressComOauthUrl(
                                            clientId,
                                            currentServer,
                                            state
                                        )
                                    } else {
                                        OauthHelper.getWordpressCustomOauthUrl(
                                            getCustomOauthHost(serviceMap)
                                                ?: "https://public-api.wordpress.com",
                                            getCustomOauthAuthorizePath(serviceMap)
                                                ?: "/oauth/authorize",
                                            clientId,
                                            currentServer,
                                            SERVICE_NAME_WORDPRESS,
                                            state,
                                            getCustomOauthScope(serviceMap) ?: "openid"
                                        )
                                    }
                                view.setupWordpressButtonListener(wordpressOauthUrl, state)
                                view.enableLoginByWordpress()
                                totalSocialAccountsEnabled++
                            }
                        }
                    }

                    // CAS account.
                    if (settings.isCasAuthenticationEnabled()) {
                        val casToken = generateRandomString(17)
                        view.setupCasButtonListener(
                            settings.casLoginUrl().casUrl(currentServer, casToken), casToken
                        )
                        view.enableLoginByCas()
                        totalSocialAccountsEnabled++
                    }

                    // Custom OAuth account.
                    getCustomOauthServices(services).let {
                        for (serviceMap in it) {
                            val serviceName = getCustomOauthServiceName(serviceMap)
                            val host = getCustomOauthHost(serviceMap)
                            val authorizePath = getCustomOauthAuthorizePath(serviceMap)
                            val clientId = getOauthClientId(serviceMap)
                            val scope = getCustomOauthScope(serviceMap)
                            val textColor = getServiceNameColorForCustomOauthOrSaml(serviceMap)
                            val buttonColor = getServiceButtonColor(serviceMap)

                            if (serviceName != null &&
                                host != null &&
                                authorizePath != null &&
                                clientId != null &&
                                scope != null &&
                                textColor != null &&
                                buttonColor != null
                            ) {
                                val customOauthUrl = OauthHelper.getCustomOauthUrl(
                                    host,
                                    authorizePath,
                                    clientId,
                                    currentServer,
                                    serviceName,
                                    state,
                                    scope
                                )

                                view.addCustomOauthButton(
                                    customOauthUrl,
                                    state,
                                    serviceName,
                                    textColor,
                                    buttonColor
                                )
                                totalSocialAccountsEnabled++
                            }
                        }
                    }

                    // SAML account.
                    getSamlServices(services).let {
                        val samlToken = generateRandomString(17)
                        for (serviceMap in it) {
                            val provider = getSamlProvider(serviceMap)
                            val serviceName = getSamlServiceName(serviceMap)
                            val textColor = getServiceNameColorForCustomOauthOrSaml(serviceMap)
                            val buttonColor = getServiceButtonColor(serviceMap)

                            if (provider != null &&
                                serviceName != null &&
                                textColor != null &&
                                buttonColor != null
                            ) {
                                view.addSamlButton(
                                    currentServer.samlUrl(provider, samlToken),
                                    samlToken,
                                    serviceName,
                                    textColor,
                                    buttonColor
                                )
                                totalSocialAccountsEnabled++
                            }
                        }
                    }

                    if (totalSocialAccountsEnabled > 0) {
                        view.showAccountsView()
                        if (totalSocialAccountsEnabled > 3) {
                            view.setupExpandAccountsView()
                        }
                    }
                }
            } catch (exception: RocketChatException) {
                Timber.e(exception)
            }
        }
    }

    private fun setupLoginWithEmailView() {
        if (settings.isLoginFormEnabled()) {
            view.showLoginWithEmailButton()
        }
    }

    private fun setupCreateNewAccountView() {
        if (settings.isRegistrationEnabledForNewUsers() && settings.isLoginFormEnabled()) {
            view.showCreateNewAccountButton()
        }
    }

    fun toCreateAccount() = navigator.toCreateAccount()

    fun toLoginWithEmail() = navigator.toLogin()

    fun authenticateWithOauth(oauthToken: String, oauthSecret: String) {
        credentialToken = oauthToken
        credentialSecret = oauthSecret
        loginMethod = AuthenticationEvent.AuthenticationWithOauth
        doAuthentication(TYPE_LOGIN_OAUTH)
    }

    fun authenticateWithCas(casToken: String) {
        credentialToken = casToken
        loginMethod = AuthenticationEvent.AuthenticationWithCas
        doAuthentication(TYPE_LOGIN_CAS)
    }

    fun authenticateWithSaml(samlToken: String) {
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
        } else {
            // If we don't have the login credentials, just go through normal setup and user input.
            setupView()
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
                            delay(3, TimeUnit.SECONDS)
                            client.loginWithCas(credentialToken)
                        }
                        TYPE_LOGIN_SAML -> {
                            delay(3, TimeUnit.SECONDS)
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


    /** Returns an OAuth service map given a [serviceName].
     *
     * @param listMap The list of [Map] to get the service from.
     * @param serviceName The service name to get in the [listMap]
     * @return The OAuth service map or null otherwise.
     */
    private fun getServiceMap(
        listMap: List<Map<String, Any>>,
        serviceName: String
    ): Map<String, Any>? = listMap.find { map -> map.containsValue(serviceName) }

    /**
     * Returns the OAuth client ID of a [serviceMap].
     * REMARK: This function works for common OAuth providers (Google, Facebook, Github and so on)
     * as well as custom OAuth.
     *
     * @param serviceMap The service map to get the OAuth client ID.
     * @return The OAuth client ID or null otherwise.
     */
    private fun getOauthClientId(serviceMap: Map<String, Any>): String? =
        serviceMap["clientId"] as? String ?: serviceMap["appId"] as? String

    /**
     * Returns a custom OAuth service list.
     *
     * @return A custom OAuth service list, otherwise an empty list if there is no custom OAuth service.
     */
    private fun getCustomOauthServices(listMap: List<Map<String, Any>>): List<Map<String, Any>> =
        listMap.filter { map -> map["custom"] == true }

    /** Returns the custom OAuth service host.
     *
     * @param serviceMap The service map to get the custom OAuth service host.
     * @return The custom OAuth service host, otherwise null.
     */
    private fun getCustomOauthHost(serviceMap: Map<String, Any>): String? =
        serviceMap["serverURL"] as? String

    /** Returns the custom OAuth service authorize path.
     *
     * @param serviceMap The service map to get the custom OAuth service authorize path.
     * @return The custom OAuth service authorize path, otherwise null.
     */
    private fun getCustomOauthAuthorizePath(serviceMap: Map<String, Any>): String? =
        serviceMap["authorizePath"] as? String

    /** Returns the custom OAuth service scope.
     *
     * @param serviceMap The service map to get the custom OAuth service scope.
     * @return The custom OAuth service scope, otherwise null.
     */
    private fun getCustomOauthScope(serviceMap: Map<String, Any>): String? =
        serviceMap["scope"] as? String

    /** Returns the text of the custom OAuth service.
     *
     * @param serviceMap The service map to get the text of the custom OAuth service.
     * @return The text of the custom OAuth service, otherwise null.
     */
    private fun getCustomOauthServiceName(serviceMap: Map<String, Any>): String? =
        serviceMap["service"] as? String

    /**
     * Returns a SAML OAuth service list.
     *
     * @return A SAML service list, otherwise an empty list if there is no SAML OAuth service.
     */
    private fun getSamlServices(listMap: List<Map<String, Any>>): List<Map<String, Any>> =
        listMap.filter { map -> map["service"] == "saml" }

    /**
     * Returns the SAML provider.
     *
     * @param serviceMap The service map to provider from.
     * @return The SAML provider, otherwise null.
     */
    private fun getSamlProvider(serviceMap: Map<String, Any>): String? =
        (serviceMap["clientConfig"] as Map<*, *>)["provider"] as? String

    /**
     * Returns the text of the SAML service.
     *
     * @param serviceMap The service map to get the text of the SAML service.
     * @return The text of the SAML service, otherwise null.
     */
    private fun getSamlServiceName(serviceMap: Map<String, Any>): String? =
        serviceMap["buttonLabelText"] as? String

    /**
     * Returns the text color of the service name.
     * REMARK: This can be used for custom OAuth or SAML.
     *
     * @param serviceMap The service map to get the text color from.
     * @return The text color of the service (custom OAuth or SAML), otherwise null.
     */
    private fun getServiceNameColorForCustomOauthOrSaml(serviceMap: Map<String, Any>): Int? =
        (serviceMap["buttonLabelColor"] as? String)?.parseColor()

    /**
     * Returns the button color of the service name.
     * REMARK: This can be used for custom OAuth or SAML.
     *
     * @param serviceMap The service map to get the button color from.
     * @return The button color of the service (custom OAuth or SAML), otherwise null.
     */
    private fun getServiceButtonColor(serviceMap: Map<String, Any>): Int? =
        (serviceMap["buttonColor"] as? String)?.parseColor()

}