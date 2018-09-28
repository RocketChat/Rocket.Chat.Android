package chat.rocket.android.authentication.server.presentation

import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.SaveConnectingServerInteractor
import chat.rocket.android.server.infraestructure.RocketChatClientFactory
import chat.rocket.android.server.presentation.CheckServerPresenter
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.isValidUrl
import javax.inject.Inject

class ServerPresenter @Inject constructor(
    private val view: ServerView,
    private val strategy: CancelStrategy,
    private val navigator: AuthenticationNavigator,
    private val serverInteractor: SaveConnectingServerInteractor,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val getAccountsInteractor: GetAccountsInteractor,
    val settingsInteractor: GetSettingsInteractor,
    val factory: RocketChatClientFactory
) : CheckServerPresenter(strategy, factory, settingsInteractor, view) {

    fun checkServer(server: String) {
        if (!server.isValidUrl()) {
            view.showInvalidServerUrlMessage()
        } else {
            view.showLoading()
            setupConnectionInfo(server)
            checkServerInfo(server)
        }
    }

    fun connect(serverUrl: String) {
        connectToServer(serverUrl) {
            if (totalSocialAccountsEnabled == 0 && !isNewAccountCreationEnabled) {
                navigator.toLogin(serverUrl)
            } else {
                navigator.toLoginOptions(
                    serverUrl,
                    state,
                    facebookOauthUrl,
                    githubOauthUrl,
                    googleOauthUrl,
                    linkedinOauthUrl,
                    gitlabOauthUrl,
                    wordpressOauthUrl,
                    casLoginUrl,
                    casToken,
                    customOauthUrl,
                    customOauthServiceName,
                    customOauthServiceNameTextColor,
                    customOauthServiceButtonColor,
                    samlUrl,
                    samlToken,
                    samlServiceName,
                    samlServiceNameTextColor,
                    samlServiceButtonColor,
                    totalSocialAccountsEnabled,
                    isLoginFormEnabled,
                    isNewAccountCreationEnabled
                )
            }
        }
    }

    fun deepLink(deepLinkInfo: LoginDeepLinkInfo) {
        connectToServer(deepLinkInfo.url) {
            navigator.toLoginOptions(deepLinkInfo.url, deepLinkInfo = deepLinkInfo)
        }
    }

    private fun connectToServer(server: String, block: () -> Unit) {
        if (!server.isValidUrl()) {
            view.showInvalidServerUrlMessage()
        } else {
            launchUI(strategy) {
                // Check if we already have an account for this server...
                val account = getAccountsInteractor.get().firstOrNull { it.serverUrl == server }
                if (account != null) {
                    navigator.toChatList(server)
                    return@launchUI
                }
                view.showLoading()
                try {
                    refreshSettingsInteractor.refresh(server)
                    serverInteractor.save(server)

                    setupConnectionInfo(server)

                    // preparing next fragment before showing it
                    checkEnabledAccounts(server)
                    checkIfLoginFormIsEnabled()
                    checkIfCreateNewAccountIsEnabled()

                    block()
                } catch (ex: Exception) {
                    view.showMessage(ex)
                } finally {
                    view.hideLoading()
                }
            }
        }
    }

}