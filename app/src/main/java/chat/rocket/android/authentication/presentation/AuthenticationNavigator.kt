package chat.rocket.android.authentication.presentation

import android.content.Intent
import chat.rocket.android.R
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.extensions.toPreviousView
import chat.rocket.android.webview.ui.webViewIntent

class AuthenticationNavigator(internal val activity: AuthenticationActivity) {

    fun toConnectWithAServer(deepLinkInfo: LoginDeepLinkInfo?) {
        activity.addFragmentBackStack(ScreenViewEvent.Server.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.server.ui.newInstance(deepLinkInfo)
        }
    }

    fun toLoginOptions(server: String, deepLinkInfo: LoginDeepLinkInfo? = null) {
        activity.addFragmentBackStack(
            ScreenViewEvent.LoginOptions.screenName,
            R.id.fragment_container
        ) {
            chat.rocket.android.authentication.loginoptions.ui.newInstance(server, deepLinkInfo)
        }
    }

    fun toTwoFA(username: String, password: String) {
        activity.addFragmentBackStack(ScreenViewEvent.TwoFa.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.twofactor.ui.newInstance(username, password)
        }
    }

    fun toCreateAccount() {
        activity.addFragmentBackStack(ScreenViewEvent.SignUp.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.signup.ui.newInstance()
        }
    }

    fun toLogin() {
        activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance()
        }
    }

    fun toForgotPassword() {
        activity.addFragmentBackStack(
            ScreenViewEvent.ResetPassword.screenName,
            R.id.fragment_container
        ) {
            chat.rocket.android.authentication.resetpassword.ui.newInstance()
        }
    }

    fun toPreviousView() {
        activity.toPreviousView()
    }

    fun toRegisterUsername(userId: String, authToken: String) {
        activity.addFragmentBackStack(
            ScreenViewEvent.RegisterUsername.screenName,
            R.id.fragment_container
        ) {
            chat.rocket.android.authentication.registerusername.ui.newInstance(userId, authToken)
        }
    }

    fun toWebPage(url: String, toolbarTitle: String? = null) {
        activity.startActivity(activity.webViewIntent(url, toolbarTitle))
        activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
    }

    fun toChatList() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    fun toChatList(serverUrl: String) {
        activity.startActivity(activity.changeServerIntent(serverUrl))
        activity.finish()
    }
}
