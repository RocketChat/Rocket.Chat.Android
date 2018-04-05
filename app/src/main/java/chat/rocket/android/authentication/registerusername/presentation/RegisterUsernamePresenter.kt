package chat.rocket.android.authentication.registerusername.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.launchUI
import chat.rocket.android.util.extensions.registerPushToken
import chat.rocket.android.util.extensions.serverLogoUrl
import chat.rocket.android.util.retryIO
import chat.rocket.common.RocketChatException
import chat.rocket.common.model.Token
import chat.rocket.common.util.ifNull
import chat.rocket.core.RocketChatClient
import chat.rocket.core.internal.rest.updateOwnBasicInformation
import javax.inject.Inject

class RegisterUsernamePresenter @Inject constructor(
    private val view: RegisterUsernameView,
    private val strategy: CancelStrategy,
    private val navigator: AuthenticationNavigator,
    private val tokenRepository: TokenRepository,
    private val localRepository: LocalRepository,
    private val factory: RocketChatClientFactory,
    private val saveAccountInteractor: SaveAccountInteractor,
    private val getAccountsInteractor: GetAccountsInteractor,
    serverInteractor: GetCurrentServerInteractor,
    settingsInteractor: GetSettingsInteractor
) {
    private val currentServer = serverInteractor.get()!!
    private val client: RocketChatClient = factory.create(currentServer)
    private var settings: PublicSettings = settingsInteractor.get(serverInteractor.get()!!)

    fun registerUsername(username: String, userId: String, authToken: String) {
        if (username.isBlank()) {
            view.alertBlankUsername()
        } else {
            launchUI(strategy) {
                if (NetworkHelper.hasInternetAccess()) {
                    view.showLoading()
                    try {
                        val me = retryIO("updateOwnBasicInformation(username = $username)") {
                            client.updateOwnBasicInformation(username = username)
                        }
                        val registeredUsername = me.username
                        if (registeredUsername != null) {
                            saveAccount(registeredUsername)
                            tokenRepository.save(currentServer, Token(userId, authToken))
                            registerPushToken()
                            navigator.toChatList()
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
                } else {
                    view.showNoInternetConnection()
                }
            }
        }
    }

    private suspend fun registerPushToken() {
        localRepository.get(LocalRepository.KEY_PUSH_TOKEN)?.let {
            client.registerPushToken(it, getAccountsInteractor.get(), factory)
        }
        // TODO: When the push token is null, at some point we should receive it with
        // onTokenRefresh() on FirebaseTokenService, we need to confirm it.
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
}