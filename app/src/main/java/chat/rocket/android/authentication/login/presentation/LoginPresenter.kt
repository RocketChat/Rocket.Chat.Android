package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.authentication.domain.model.TokenModel
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.encodeToBase64
import chat.rocket.android.util.extensions.generateRandomString
import chat.rocket.android.util.extensions.isEmail
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.Token
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.*
import chat.rocket.core.model.Myself
import kotlinx.coroutines.experimental.delay
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val TYPE_LOGIN_USER_EMAIL = 0
private const val TYPE_LOGIN_CAS = 1
private const val TYPE_LOGIN_OAUTH = 2
private const val SERVICE_NAME_GITHUB = "github"
private const val SERVICE_NAME_GOOGLE = "google"
private const val SERVICE_NAME_LINKEDIN = "linkedin"
private const val SERVICE_NAME_GILAB = "gitlab"

class LoginPresenter @Inject constructor(private val view: LoginView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator,
                                         private val tokenRepository: TokenRepository,
                                         private val localRepository: LocalRepository,
                                         private val getAccountsInteractor: GetAccountsInteractor,
                                         settingsInteractor: GetSettingsInteractor,
                                         serverInteractor: GetCurrentServerInteractor,
                                         private val saveAccountInteractor: SaveAccountInteractor,
                                         private val factory: RocketChatClientFactory) {
    // TODO - we should validate the current server when opening the app, and have a nonnull get()
    private val currentServer = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(currentServer)
    private val settings: PublicSettings = settingsInteractor.get(currentServer)
    private lateinit var usernameOrEmail: String
    private lateinit var password: String
    private lateinit var credentialToken: String
    private lateinit var credentialSecret: String

    fun setupView() {
        setupLoginView()
        setupUserRegistrationView()
        setupCasView()
        setupOauthServicesView()
    }

    fun authenticateWithUserAndPassword(usernameOrEmail: String, password: String) {
        when {
            usernameOrEmail.isBlank() -> {
                view.alertWrongUsernameOrEmail()
            }
            password.isEmpty() -> {
                view.alertWrongPassword()
            }
            else -> {
                this.usernameOrEmail = usernameOrEmail
                this.password = password
                doAuthentication(TYPE_LOGIN_USER_EMAIL)
            }
        }
    }

    fun authenticateWithCas(token: String) {
        credentialToken = token
        doAuthentication(TYPE_LOGIN_CAS)
    }

    fun authenticateWithOauth(token: String, secret: String) {
        credentialToken = token
        credentialSecret = secret
        doAuthentication(TYPE_LOGIN_OAUTH)
    }

    fun signup() = navigator.toSignUp()

    private fun setupLoginView() {
        if (settings.isLoginFormEnabled()) {
            view.showFormView()
            view.setupLoginButtonListener()
            view.setupGlobalListener()
        } else {
            view.hideFormView()
        }
    }

    private fun setupCasView() {
        if (settings.isCasAuthenticationEnabled()) {
            val token = generateRandomString(17)
            view.setupCasButtonListener(UrlHelper.getCasUrl(settings.casLoginUrl(), currentServer, token), token)
            view.showCasButton()
        }
    }

    private fun setupUserRegistrationView() {
        if (settings.isRegistrationEnabledForNewUsers()) {
            view.showSignUpView()
            view.setupSignUpView()
        }
    }

    private fun setupOauthServicesView() {
        launchUI(strategy) {
            try {
                val services = client.settingsOauth().services
                val state = "{\"loginStyle\":\"popup\",\"credentialToken\":\"${generateRandomString(40)}\",\"isCordova\":true}".encodeToBase64()

                if (services.isNotEmpty()) {
                    var totalSocialAccountsEnabled = 0

                    if (settings.isFacebookAuthenticationEnabled()) {
                        view.enableLoginByFacebook()
                        totalSocialAccountsEnabled++
                    }
                    if (settings.isGithubAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_GITHUB)
                        if (clientId != null) {
                            view.setupGithubButtonListener(UrlHelper.getGithubOauthUrl(clientId, state), state)
                            view.enableLoginByGithub()
                            totalSocialAccountsEnabled++
                        }
                    }
                    if (settings.isGoogleAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_GOOGLE)
                        if (clientId != null) {
                            view.setupGoogleButtonListener(UrlHelper.getGoogleOauthUrl(clientId, currentServer, state), state)
                            view.enableLoginByGoogle()
                            totalSocialAccountsEnabled++
                        }
                    }
                    if (settings.isLinkedinAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_LINKEDIN)
                        if (clientId != null) {
                            view.setupGoogleButtonListener(UrlHelper.getLinkedinOauthUrl(clientId, currentServer, state), state)
                            view.enableLoginByLinkedin()
                            totalSocialAccountsEnabled++
                        }
                    }
                    if (settings.isMeteorAuthenticationEnabled()) {
                        view.enableLoginByMeteor()
                        totalSocialAccountsEnabled++
                    }
                    if (settings.isTwitterAuthenticationEnabled()) {
                        view.enableLoginByTwitter()
                        totalSocialAccountsEnabled++
                    }
                    if (settings.isGitlabAuthenticationEnabled()) {
                        val clientId = getOauthClientId(services, SERVICE_NAME_GILAB)
                        if (clientId != null) {
                            view.setupGitlabButtonListener(UrlHelper.getGitlabOauthUrl(clientId, currentServer, state), state)
                            view.enableLoginByGitlab()
                            totalSocialAccountsEnabled++
                        }
                    }

                    if (totalSocialAccountsEnabled > 0) {
                        view.showOauthView()
                        if (totalSocialAccountsEnabled > 3) {
                            view.setupFabListener()
                        }
                    } else {
                        view.hideOauthView()
                    }
                } else {
                    view.hideOauthView()
                }
            } catch (exception: RocketChatException) {
                Timber.e(exception)
                view.hideOauthView()
            }
        }
    }

    private fun doAuthentication(loginType: Int) {
        launchUI(strategy) {
            if (NetworkHelper.hasInternetAccess()) {
                view.disableUserInput()
                view.showLoading()
                try {
                    val token = when (loginType) {
                        TYPE_LOGIN_USER_EMAIL -> {
                            if (usernameOrEmail.isEmail()) {
                                client.loginWithEmail(usernameOrEmail, password)
                            } else {
                                if (settings.isLdapAuthenticationEnabled()) {
                                    client.loginWithLdap(usernameOrEmail, password)
                                } else {
                                    client.login(usernameOrEmail, password)
                                }
                            }
                        }
                        TYPE_LOGIN_CAS -> {
                            delay(3, TimeUnit.SECONDS)
                            client.loginWithCas(credentialToken)
                        }
                        TYPE_LOGIN_OAUTH -> {
                            client.loginWithOauth(credentialToken, credentialSecret)
                        }
                        else -> {
                            throw IllegalStateException("Expected TYPE_LOGIN_USER_EMAIL, TYPE_LOGIN_CAS or TYPE_LOGIN_OAUTH")
                        }
                    }
                    val me = client.me()
                    localRepository.save(LocalRepository.CURRENT_USERNAME_KEY, me.username)
                    saveAccount(me)
                    saveToken(token)
                    registerPushToken()
                    navigator.toChatList()
                } catch (exception: RocketChatException) {
                    exception.message?.let {
                        view.showMessage(it)
                    }.ifNull {
                        view.showGenericErrorMessage()
                    }
                } finally {
                    view.hideLoading()
                    view.enableUserInput()
                }
            } else {
                view.showNoInternetConnection()
            }
        }
    }

    private fun saveToken(token: Token) {
        tokenRepository.save(currentServer, token)
    }

    private suspend fun registerPushToken() {
        localRepository.get(LocalRepository.KEY_PUSH_TOKEN)?.let {
            client.registerPushToken(it, getAccountsInteractor.get(), factory)
        }
        // TODO: When the push token is null, at some point we should receive it with
        // onTokenRefresh() on FirebaseTokenService, we need to confirm it.
    }

    private fun getOauthClientId(listMap: List<Map<String, String>>, serviceName: String): String? {
        return listMap.find { map -> map.containsValue(serviceName) }
                ?.get("appId")
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