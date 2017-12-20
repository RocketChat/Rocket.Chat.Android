package chat.rocket.android.authentication.server.presentation

import android.content.Context
import chat.rocket.android.R
import chat.rocket.android.authentication.presentation.AuthenticationNavigator
import chat.rocket.android.core.lifecycle.CancelStrategy
import chat.rocket.android.helper.NetworkHelper
import chat.rocket.android.util.launchUI
import javax.inject.Inject

class ServerPresenter @Inject constructor(private val view: ServerView,
                                          private val strategy: CancelStrategy,
                                          private val navigator: AuthenticationNavigator) {

    fun login(context: Context, server: String) {
        launchUI(strategy) {
            if (NetworkHelper.hasInternetAccess()) {
                view.showLoading()

                // TODO - validate server URL and get server settings and info before going to Login screen

                view.hideLoading()
                navigator.toLogin(server)
            } else {
                view.showMessage(context.getString(R.string.msg_no_internet_connection))
            }
        }
    }
}