package chat.rocket.android.authentication.onboarding.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.SaveConnectingServerInteractor
import chat.rocket.android.util.extension.launchUI
import chat.rocket.android.util.extensions.isValidUrl
import javax.inject.Inject

class OnBoardingPresenter @Inject constructor(
        private val view: OnBoardingView,
        private val strategy: CancelStrategy,
        private val navigator: AuthenticationNavigator,
        private val serverInteractor: SaveConnectingServerInteractor,
        private val refreshSettingsInteractor: RefreshSettingsInteractor,
        private val getAccountsInteractor: GetAccountsInteractor
) {
    fun createServer(){
        navigator.toWebPage("https://cloud.rocket.chat/trial")
    }

    fun connect(server: String) {
        //code that leads to login screen (smart lock will be implemented after this)
        connectToServer(server) {
            navigator.toLoginOptions(server)
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