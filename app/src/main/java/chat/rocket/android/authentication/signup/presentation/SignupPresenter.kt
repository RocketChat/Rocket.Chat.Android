package chat.rocket.android.authentication.signup.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.main.viewmodel.NavHeaderViewModel
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.common.RocketChatException
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.login
import chat.rocket.core.internal.rest.me
import chat.rocket.core.internal.rest.registerPushToken
import chat.rocket.core.internal.rest.signup
import chat.rocket.core.model.Myself
import javax.inject.Inject

class SignupPresenter @Inject constructor(private val view: SignupView,
                                          private val strategy: CancelStrategy,
                                          private val navigator: AuthenticationNavigator,
                                          private val localRepository: LocalRepository,
                                          private val serverInteractor: GetCurrentServerInteractor,
                                          private val factory: RocketChatClientFactory,
                                          private val saveAccountInteractor: SaveAccountInteractor,
                                          private val getAccountsInteractor: GetAccountsInteractor,
                                          settingsInteractor: GetSettingsInteractor) {
    private val currentServer = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(currentServer)
    private var settings: PublicSettings = settingsInteractor.get(serverInteractor.get()!!)

    fun signup(name: String, username: String, password: String, email: String) {
        val server = serverInteractor.get()
        when {
            server == null -> {
                navigator.toServerScreen()
            }
            name.isBlank() -> {
                view.alertBlankName()
            }
            username.isBlank() -> {
                view.alertBlankUsername()
            }
            password.isEmpty() -> {
                view.alertEmptyPassword()
            }
            email.isBlank() -> {
                view.alertBlankEmail()
            }
            else -> {
                val client = factory.create(server)
                launchUI(strategy) {
                    if (NetworkHelper.hasInternetAccess()) {
                        view.showLoading()

                        try {
                            // TODO This function returns a user so should we save it?
                            client.signup(email, name, username, password)
                            // TODO This function returns a user token so should we save it?
                            client.login(username, password)
                            val me = client.me()
                            localRepository.save(LocalRepository.CURRENT_USERNAME_KEY, me.username)
                            saveAccount(me)
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

                        }
                    } else {
                        view.showNoInternetConnection()
                    }
                }
            }
        }
    }

    fun termsOfService() {
        serverInteractor.get()?.let {
            navigator.toWebPage(UrlHelper.getTermsOfServiceUrl(it))
        }
    }

    fun privacyPolicy() {
        serverInteractor.get()?.let {
            navigator.toWebPage(UrlHelper.getPrivacyPolicyUrl(it))
        }
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