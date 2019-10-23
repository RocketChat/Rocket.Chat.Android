package chat.rocket.android.authentication.registerusername.presentation

import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.AuthenticationEvent
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetConnectingServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.PublicSettings
import chat.rocket.android.server.domain.SaveAccountInteractor
import chat.rocket.android.server.domain.SaveCurrentServerInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.favicon
import chat.rocket.android.server.domain.model.Account
import chat.rocket.android.server.domain.siteName
import chat.rocket.android.server.domain.wideTile
import chat.rocket.android.server.infrastructure.RocketChatClientFactory
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.avatarUrl
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
    private val saveAccountInteractor: SaveAccountInteractor,
    private val analyticsManager: AnalyticsManager,
    private val saveCurrentServer: SaveCurrentServerInteractor,
    val serverInteractor: GetConnectingServerInteractor,
    val factory: RocketChatClientFactory,
    val settingsInteractor: GetSettingsInteractor
) {
    private val currentServer = serverInteractor.get()!!
    private val client: RocketChatClient = factory.get(currentServer)
    private var settings: PublicSettings = settingsInteractor.get(currentServer)
    private val token = tokenRepository.get(currentServer)

    fun registerUsername(username: String, userId: String, authToken: String) {
        launchUI(strategy) {
            view.showLoading()
            try {
                val me = retryIO("updateOwnBasicInformation(username = $username)") {
                    client.updateOwnBasicInformation(username = username)
                }
                val registeredUsername = me.username
                if (registeredUsername != null) {
                    saveAccount(registeredUsername)
                    saveCurrentServer.save(currentServer)
                    tokenRepository.save(currentServer, Token(userId, authToken))
                    analyticsManager.logSignUp(
                        AuthenticationEvent.AuthenticationWithOauth,
                        true
                    )
                    navigator.toChatList()
                }
            } catch (exception: RocketChatException) {
                analyticsManager.logSignUp(AuthenticationEvent.AuthenticationWithOauth, false)
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

    private fun saveAccount(username: String) {
        val icon = settings.favicon()?.let {
            currentServer.serverLogoUrl(it)
        }
        val logo = settings.wideTile()?.let {
            currentServer.serverLogoUrl(it)
        }
        val thumb = currentServer.avatarUrl(username, token?.userId, token?.authToken)
        val account = Account(
            serverName = settings.siteName() ?: currentServer,
            serverUrl = currentServer,
            serverLogoUrl = icon,
            serverBackgroundImageUrl = logo,
            userName = username,
            userAvatarUrl = thumb,
            authToken = token?.authToken,
            userId = token?.userId
        )
        saveAccountInteractor.save(account)
    }
}