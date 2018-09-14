package chat.rocket.android.authentication.loginoptions.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.authentication.login.presentation.*
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.OauthHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.*
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatAuthException
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatTwoFactorException
import chat.rocket.common.model.Email
import chat.rocket.common.model.Token
import chat.rocket.common.model.User
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.*
import kotlinx.coroutines.experimental.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TYPE_LOGIN_OAUTH = 3
private const val SERVICE_NAME_FACEBOOK = "facebook"
private const val SERVICE_NAME_GITHUB = "github"
private const val SERVICE_NAME_GOOGLE = "google"
private const val SERVICE_NAME_LINKEDIN = "linkedin"
private const val SERVICE_NAME_GILAB = "gitlab"

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
    private lateinit var settings: PublicSettings
    private lateinit var client: RocketChatClient
    private lateinit var credentialToken: String
    private lateinit var credentialSecret: String

    fun setupView(){
        setupConnectionInfo(currentServer)
        setupOauthServicesView()
    }

    private fun setupConnectionInfo(serverUrl: String) {
        currentServer = serverUrl
        client = factory.create(serverUrl)
        settings = settingsInteractor.get(serverUrl)
    }

    private fun setupOauthServicesView() {
        launchUI(strategy) {
            try {
                val services = retryIO("settingsOauth()") {
                    client.settingsOauth().services
                }
                if (services.isNotEmpty()) {
                    val state =
                            "{\"loginStyle\":\"popup\",\"credentialToken\":\"${generateRandomString(40)}\",\"isCordova\":true}".encodeToBase64()
                    var totalSocialAccountsEnabled = 0

                    if (settings.isFacebookAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_FACEBOOK)
                        if (clientId != null) {
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
                    if (settings.isGithubAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_GITHUB)
                        if (clientId != null) {
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
                    if (settings.isGoogleAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_GOOGLE)
                        if (clientId != null) {
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
                    if (settings.isLinkedinAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_LINKEDIN)
                        if (clientId != null) {
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
                    if (settings.isGitlabAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_GILAB)
                        if (clientId != null) {
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

//                    getCustomOauthServices(services).let {
//                        for (service in it) {
//                            val serviceName = getCustomOauthServiceName(service)
//
//                            val customOauthUrl = OauthHelper.getCustomOauthUrl(
//                                    getCustomOauthHost(service),
//                                    getCustomOauthAuthorizePath(service),
//                                    getCustomOauthClientId(service),
//                                    currentServer,
//                                    serviceName,
//                                    state,
//                                    getCustomOauthScope(service)
//                            )
//
//                            view.addCustomOauthServiceButton(
//                                    customOauthUrl,
//                                    state,
//                                    serviceName,
//                                    getServiceNameColor(service),
//                                    getServiceButtonColor(service)
//                            )
//                            totalSocialAccountsEnabled++
//                        }
//                    }

//                    getSamlServices(services).let {
//                        val samlToken = generateRandomString(17)
//
//                        for (service in it) {
//                            view.addSamlServiceButton(
//                                    currentServer.samlUrl(getSamlProvider(service), samlToken),
//                                    samlToken,
//                                    getSamlServiceName(service),
//                                    getServiceNameColor(service),
//                                    getServiceButtonColor(service)
//                            )
//                            totalSocialAccountsEnabled++
//                        }
//                    }

//                    if (totalSocialAccountsEnabled > 0) {
//                        view.enableOauthView()
//                        if (totalSocialAccountsEnabled > 3) {
//                            view.setupFabListener()
//                        }
//                    } else {
//                        view.disableOauthView()
//                    }
                } else {
//                    view.disableOauthView()
                }
            } catch (exception: RocketChatException) {
                Timber.e(exception)
            }
        }
    }

    fun authenticateWithOauth(oauthToken: String, oauthSecret: String) {
        credentialToken = oauthToken
        credentialSecret = oauthSecret
        doAuthentication(TYPE_LOGIN_OAUTH)
    }

    private fun doAuthentication(loginType: Int) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val token = retryIO("login") {
                    when (loginType) {

                        TYPE_LOGIN_OAUTH -> {
                            client.loginWithOauth(credentialToken, credentialSecret)
                        }
                        else -> {
                            throw IllegalStateException("Expected TYPE_LOGIN_USER_EMAIL, TYPE_LOGIN_CAS,TYPE_LOGIN_SAML, TYPE_LOGIN_OAUTH or TYPE_LOGIN_DEEP_LINK")
                        }
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
                    localRepository.saveCurrentUser(url = currentServer, user = user)
                    saveCurrentServer.save(currentServer)
                    saveAccount(myself.username!!)
                    saveToken(token)
                    // TODO
//                    analyticsManager.logSignUp(
//                        AuthenticationEvent.AuthenticationWithOauth,
//                        true
//                    )
                    navigator.toChatList()
                } else if (loginType == TYPE_LOGIN_OAUTH) {
                    navigator.toRegisterUsername(token.userId, token.authToken)
                }
            } catch (exception: RocketChatException) {
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

    private fun getOauthClientId(listMap: List<Map<String, Any>>, serviceName: String): String? {
        return listMap.find { map -> map.containsValue(serviceName) }?.let {
            it["clientId"] ?: it["appId"]
        }.toString()
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

    private fun saveToken(token: Token) {
        tokenRepository.save(currentServer, token)
    }

    fun toCreateAccount() {
        navigator.toCreateAccount()
    }

    fun toLogin() {
        navigator.toLogin()
    }
}