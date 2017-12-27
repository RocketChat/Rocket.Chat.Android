package chat.rocket.android.authentication.presentation

import android.content.Context
import android.content.Intent
import chat.rocket.android.R
import chat.rocket.android.chatrooms.ui.MainActivity
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.app.MainActivity
import chat.rocket.android.authentication.login.ui.LoginFragment
import chat.rocket.android.authentication.signup.ui.SignupFragment
import chat.rocket.android.authentication.twofactor.ui.TwoFAFragment
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.addFragmentBackStack
import chat.rocket.android.webview.webViewIntent

class AuthenticationNavigator(internal val activity: AuthenticationActivity, internal val context: Context) {
    var currentServer: String? = null

    fun toLogin(server: String) {
        currentServer = server
        activity.addFragmentBackStack("loginFragment", R.id.fragment_container) {
            LoginFragment.newInstance(server)
        }
    }

    fun toTwoFA(server: String, username: String, password: String) {
        currentServer = server
        activity.addFragmentBackStack("twoFAFragment", R.id.fragment_container) {
            TwoFAFragment.newInstance(server, username, password)
        }
    }

    fun toSignUp(server: String) {
        currentServer = server
        activity.addFragmentBackStack("signupFragment", R.id.fragment_container) {
            SignupFragment.newInstance(server)
        }
    }

    fun toTermsOfService() {
        val webPageUrl = currentServer + "/terms-of-service"
        activity.startActivity(context.webViewIntent(webPageUrl))
    }

    fun toPrivacyPolicy() {
        val webPageUrl = currentServer + "/privacy-policy"
        activity.startActivity(context.webViewIntent(webPageUrl))
    }

    fun toChatList() {
        val chatList = Intent(activity, MainActivity::class.java).apply {
            //TODO any parameter to pass
        }
        activity.startActivity(chatList)
        activity.finish()
    }
}
