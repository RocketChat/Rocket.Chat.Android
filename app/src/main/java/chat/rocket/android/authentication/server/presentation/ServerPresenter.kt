package chat.rocket.android.authentication.server.presentation

import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetAccountsInteractor
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
    factory: RocketChatClientFactory
) : CheckServerPresenter(strategy, factory, view) {

    fun checkServer(server: String) {
        if (!server.isValidUrl()) {
            view.showInvalidServerUrlMessage()
        } else {
            view.showLoading()
            checkServerInfo(server)
        }
    }

    fun connect(server: String) {
        connectToServer(server) { navigator.toLoginOptions(server) }
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
                    block()
                } catch (ex: Exception) {
                    view.showMessage(ex)
                } finally {
                    view.hideLoading()
                }
            }
        }
    }

    fun deepLink(deepLinkInfo: LoginDeepLinkInfo) {
        connectToServer(deepLinkInfo.url) {
            navigator.toLoginOptions(deepLinkInfo.url, deepLinkInfo)
        }
    }
}