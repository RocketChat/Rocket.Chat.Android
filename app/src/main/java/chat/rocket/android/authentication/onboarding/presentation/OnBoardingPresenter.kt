package chat.rocket.android.authentication.onboarding.presentation

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
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class OnBoardingPresenter @Inject constructor(
    private val view: OnBoardingView,
    private val strategy: CancelStrategy,
    private val navigator: AuthenticationNavigator,
    private val serverInteractor: SaveConnectingServerInteractor,
    private val refreshSettingsInteractor: RefreshSettingsInteractor,
    private val getAccountsInteractor: GetAccountsInteractor,
    val settingsInteractor: GetSettingsInteractor,
    val factory: RocketChatClientFactory
) : CheckServerPresenter(strategy, factory, settingsInteractor) {

    fun toSignInToYourServer() = navigator.toSignInToYourServer()

    fun connectToCommunityServer(communityServerUrl: String) {
        connectToServer(communityServerUrl) {
            if (totalSocialAccountsEnabled == 0 && !isNewAccountCreationEnabled) {
                navigator.toLogin(communityServerUrl)
            } else {
                navigator.toLoginOptions(
                    communityServerUrl,
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

    fun toCreateANewServer(createServerUrl: String) = navigator.toWebPage(createServerUrl)

    private fun connectToServer(serverUrl: String, block: () -> Unit) {
        launchUI(strategy) {
            // Check if we already have an account for this server...
            val account = getAccountsInteractor.get().firstOrNull { it.serverUrl == serverUrl }
            if (account != null) {
                navigator.toChatList(serverUrl)
                return@launchUI
            }
            view.showLoading()
            try {
                withContext(DefaultDispatcher) {
                    refreshSettingsInteractor.refresh(serverUrl)

                    setupConnectionInfo(serverUrl)

                    // preparing next fragment before showing it
                    checkEnabledAccounts(serverUrl)
                    checkIfLoginFormIsEnabled()
                    checkIfCreateNewAccountIsEnabled()

                    serverInteractor.save(serverUrl)

                    block()
                }
            } catch (ex: Exception) {
                view.showMessage(ex)
            } finally {
                view.hideLoading()
            }
        }
    }
}