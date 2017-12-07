package chat.rocket.android.authentication.presentation

import javax.inject.Inject

class ServerPresenter @Inject constructor(private val view: ServerView,
                                          private val navigator: AuthenticationNavigator) {

    fun login(server: String) {
        // TODO - validate server URL and get server settings and info before going to Login screen
        navigator.toLogin(server)
    }
}