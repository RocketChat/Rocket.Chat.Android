package chat.rocket.android.authentication.onboarding.presentation

import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.behaviours.showMessage
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.server.domain.GetAccountsInteractor
import chat.rocket.android.server.domain.RefreshSettingsInteractor
import chat.rocket.android.server.domain.SaveConnectingServerInteractor
import chat.rocket.android.util.extension.launchUI
import javax.inject.Inject

class OnBoardingPresenter @Inject constructor(
    private val view: OnBoardingView,
    private val strategy: CancelStrategy,
    private val navigator: AuthenticationNavigator,
    private val serverInteractor: SaveConnectingServerInteractor,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val getAccountsInteractor: GetAccountsInteractor
) {

    fun toConnectWithAServer(deepLinkInfo: LoginDeepLinkInfo?) =
        navigator.toConnectWithAServer(deepLinkInfo)

    fun connectToCommunityServer(communityServer: String) =
        connectToServer(communityServer) { navigator.toLoginOptions(communityServer) }

    fun toCreateANewServer(createServerUrl: String) = navigator.toWebPage(createServerUrl)

    private fun connectToServer(server: String, block: () -> Unit) {
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