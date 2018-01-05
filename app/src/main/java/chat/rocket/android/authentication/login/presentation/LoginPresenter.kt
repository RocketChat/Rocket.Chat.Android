package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.launchUI
import chat.rocket.common.RocketChatException
import chat.rocket.common.RocketChatTwoFactorException
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import timber.log.Timber
import javax.inject.Inject

class LoginPresenter @Inject constructor(private val view: LoginView,
                                         private val strategy: CancelStrategy,
                                         private val navigator: AuthenticationNavigator,
                                         private val settingsInteractor: GetSettingsInteractor,
                                         private val serverInteractor: GetCurrentServerInteractor,
                                         factory: RocketChatClientFactory) {

    // TODO - we should validate the current server when opening the app, and have a nonnull get()
    private val client: RocketChatClient = factory.create(serverInteractor.get()!!)

    fun setup() {
        val server = serverInteractor.get()
        if (server == null) {
            navigator.toServerScreen()
            return
        }

        val settings = settingsInteractor.get(server)
        if (settings == null) {
            navigator.toServerScreen()
            return
        }

        var hasSocial = false
        if (settings.facebookEnabled()) {
            view.enableLoginByFacebook()
            hasSocial = true
        }
        if (settings.githubEnabled()) {
            view.enableLoginByGithub()
            hasSocial = true
        }
        if (settings.googleEnabled()) {
            view.enableLoginByGoogle()
            hasSocial = true
        }
        if (settings.linkedinEnabled()) {
            view.enableLoginByLinkedin()
            hasSocial = true
        }
        if (settings.meteorEnabled()) {
            view.enableLoginByMeteor()
            hasSocial = true
        }
        if (settings.twitterEnabled()) {
            view.enableLoginByTwitter()
            hasSocial = true
        }
        if (settings.gitlabEnabled()) {
            view.enableLoginByGitlab()
            hasSocial = true
        }

        view.showSignUpView(settings.registrationEnabled())
        view.showOauthView(hasSocial)
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
                    view.showLoading()
                    if (NetworkHelper.hasInternetAccess()) {
                        try {
                            val token = client.login(usernameOrEmail, password)
                            Timber.d("Created token: $token")
                            navigator.toChatList()
                        } catch (rocketChatException: RocketChatException) {
                            when (rocketChatException) {
                                is RocketChatTwoFactorException -> {
                                    navigator.toTwoFA(usernameOrEmail, password)
                                }
                                else -> {
                                    val errorMessage = rocketChatException.message
                                    if (errorMessage != null) {
                                        view.showMessage(errorMessage)
                                    }
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

    fun signup() {
        // TODO - remove the server parameter here, signup screen should use the interactor too
        navigator.toSignUp()
    }
}