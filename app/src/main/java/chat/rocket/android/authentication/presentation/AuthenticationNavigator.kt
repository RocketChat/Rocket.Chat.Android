package chat.rocket.android.authentication.presentation

import android.content.Context
import android.content.Intent
import chat.rocket.android.R
import chat.rocket.android.app.MainActivity
import chat.rocket.android.authentication.login.ui.LoginFragment
import chat.rocket.android.authentication.signup.ui.SignupFragment
import chat.rocket.android.authentication.twofactor.ui.TwoFAFragment
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.addFragmentBackStack
import chat.rocket.android.webview.webViewIntent

class AuthenticationNavigator(internal val activity: AuthenticationActivity, internal val context: Context) {
    lateinit var server: String
    lateinit var usernameOrEmail: String
    lateinit var password: String

    fun toLogin(server: String) {
        this.server = server
        activity.addFragmentBackStack("loginFragment", R.id.fragment_container) {
            LoginFragment.newInstance()
        }
    }

    fun toTwoFA(usernameOrEmail: String, password: String) {
        this.usernameOrEmail = usernameOrEmail
        this.password = password
        activity.addFragmentBackStack("twoFAFragment", R.id.fragment_container) {
            TwoFAFragment.newInstance()
        }
    }

    fun toSignUp() {
        activity.addFragmentBackStack("signupFragment", R.id.fragment_container) {
            SignupFragment.newInstance()
        }
    }

    fun toTermsOfService() {
        val webPageUrl = server + "/terms-of-service" // TODO Move to UrlHelper
        activity.startActivity(context.webViewIntent(webPageUrl))
        activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
    }

    fun toPrivacyPolicy() {
        val webPageUrl = server + "/privacy-policy" // TODO Move to UrlHelper
        activity.startActivity(context.webViewIntent(webPageUrl))
        activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
    }

    fun toChatList() {
        val chatRoom = Intent(activity, MainActivity::class.java).apply {
            //TODO any parameter to pass
        }
        activity.startActivity(chatRoom)
        activity.finish()
    }
}
