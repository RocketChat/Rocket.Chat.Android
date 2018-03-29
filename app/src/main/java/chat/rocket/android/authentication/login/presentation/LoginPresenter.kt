package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.authentication.domain.model.TokenModel
import chat.rocket.android.authentication.domain.model.toToken
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.generateRandomString
import chat.rocket.android.util.extensions.isEmailValid
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatTwoFactorException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.*
import chat.rocket.core.model.Myself
import kotlinx.coroutines.experimental.delay
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val view: LoginView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator,
                                         private val tokenRepository: TokenRepository,
                                         private val localRepository: LocalRepository,
                                         private val getAccountsInteractor: GetAccountsInteractor,
                                         private val settingsInteractor: GetSettingsInteractor,
                                         private val serverInteractor: GetCurrentServerInteractor,
                                         private val saveAccountInteractor: SaveAccountInteractor,
                                         private val factory: RocketChatClientFactory) {
    // TODO - we should validate the current server when opening the app, and have a nonnull get()
    private val currentServer = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(currentServer)
    private var settings: PublicSettings = settingsInteractor.get(serverInteractor.get()!!)

    fun setupView() {
        val server = serverInteractor.get()
        if (server == null) {
            navigator.toServerScreen()
            return
        }
        val settings = settingsInteractor.get(server)

        if (settings.isLoginFormEnabled()) {
            view.showFormView()
            view.setupLoginButtonListener()
            view.setupGlobalListener()
        } else {
            view.hideFormView()
        }

        if (settings.isRegistrationEnabledForNewUsers()) {
            view.showSignUpView()
            view.setupSignUpView()
        }

        if (settings.isCasAuthenticationEnabled()) {
            val token = generateRandomString(17)
            view.setupCasButtonListener(UrlHelper.getCasUrl(settings.casLoginUrl(), server, token), token)
            view.showCasButton()
        }

        var totalSocialAccountsEnabled = 0
        if (settings.isFacebookAuthenticationEnabled()) {
            view.enableLoginByFacebook()
            totalSocialAccountsEnabled++
        }
        if (settings.isGithubAuthenticationEnabled()) {
            view.enableLoginByGithub()
            totalSocialAccountsEnabled++
        }
        if (settings.isGoogleAuthenticationEnabled()) {
            view.enableLoginByGoogle()
            totalSocialAccountsEnabled++
        }
        if (settings.isLinkedinAuthenticationEnabled()) {
            view.enableLoginByLinkedin()
            totalSocialAccountsEnabled++
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
            view.enableLoginByGitlab()
            totalSocialAccountsEnabled++
        }

        if (totalSocialAccountsEnabled > 0) {
            view.showOauthView()
            if (totalSocialAccountsEnabled > 3) {
                view.setupFabListener()
            }
        }
    }

    fun authenticate(usernameOrEmail: String, password: String) {
        val server = serverInteractor.get()
        when {
            server == null -> {
                navigator.toServerScreen()
            }
            usernameOrEmail.isBlank() -> {
                view.alertWrongUsernameOrEmail()
            }
            password.isEmpty() -> {
                view.alertWrongPassword()
            }
            else -> {
                launchUI(strategy) {
                    if (NetworkHelper.hasInternetAccess()) {
                        view.disableUserInput()
                        view.showLoading()
                        try {
                            val token = if (usernameOrEmail.isEmailValid()) {
                                client.loginWithEmail(usernameOrEmail, password)
                            } else {
                                val settings = settingsInteractor.get(server)
                                if (settings.isLdapAuthenticationEnabled()) {
                                    client.loginWithLdap(usernameOrEmail, password)
                                } else {
                                    client.login(usernameOrEmail, password)
                                }
                            }

                            val me = client.me()
                            saveToken(server, TokenModel(token.userId, token.authToken), me.username)
                            saveAccount(me)
                            registerPushToken()
                            navigator.toChatList()
                        } catch (exception: RocketChatException) {
                            when (exception) {
                                is RocketChatTwoFactorException -> {
                                    navigator.toTwoFA(usernameOrEmail, password)
                                }
                                else -> {
                                    exception.message?.let {
                                        view.showMessage(it)
                                    }.ifNull {
                                        view.showGenericErrorMessage()
                                    }
                                }
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
        }
    }

    fun authenticateWithCas(casToken: String) {
        launchUI(strategy) {
            if (NetworkHelper.hasInternetAccess()) {
                view.disableUserInput()
                view.showLoading()
                try {
                    val server = serverInteractor.get()
                    if (server != null) {
                        delay(3, TimeUnit.SECONDS)
                        val token = client.loginWithCas(casToken)
                        val me = client.me()
                        saveToken(server, TokenModel(token.userId, token.authToken), me.username)
                        saveAccount(me)
                        registerPushToken()
                        navigator.toChatList()
                    } else {
                        navigator.toServerScreen()
                    }
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

    fun signup() = navigator.toSignUp()

    private suspend fun saveToken(server: String, tokenModel: TokenModel, username: String?) {
        localRepository.save(LocalRepository.CURRENT_USERNAME_KEY, username)
        tokenRepository.save(server, tokenModel.toToken())
        registerPushToken()
    }

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