package chat.rocket.android.authentication.server.presentation

import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.util.launchUI
import chat.rocket.core.RocketChatClient
import javax.inject.Inject

class ServerPresenter @Inject constructor(private val view: ServerView,
                                          private val strategy: CancelStrategy,
                                          private val navigator: AuthenticationNavigator) {
    @Inject lateinit var client: RocketChatClient

    fun connect(server: String) {
        launchUI(strategy) {
            if (NetworkHelper.hasInternetAccess()) {
                view.showLoading()

                // TODO - validate server URL and get server settings and info before going to Login screen
                //client.connect(server)
                navigator.toLogin(server)

                view.hideLoading()
            } else {
                view.showNoInternetConnection()
            }
        }
    }
}