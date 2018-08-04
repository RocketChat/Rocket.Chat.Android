package chat.rocket.android.authentication.presentation

import android.content.Intent
import androidx.appcompat.widget.DialogTitle
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.login.ui.LoginFragment
import chat.rocket.android.authentication.loginoptions.ui.LoginOptionsFragment
import chat.rocket.android.authentication.registerusername.ui.RegisterUsernameFragment
import chat.rocket.android.authentication.resetpassword.ui.ResetPasswordFragment
import chat.rocket.android.authentication.signup.ui.SignupFragment
import chat.rocket.android.authentication.twofactor.ui.TwoFAFragment
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.authentication.ui.newServerIntent
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.util.extensions.toPreviousView
import chat.rocket.android.webview.ui.webViewIntent

class AuthenticationNavigator(internal val activity: AuthenticationActivity) {

    fun toLoginOptions(server: String) {
        activity.addFragmentBackStack("LoginOptionFragment", R.id.fragment_container) {
            LoginOptionsFragment.newInstance(server)
        }
    }

    fun toLogin() {
        activity.addFragmentBackStack("LoginFragment", R.id.fragment_container) {
            LoginFragment.newInstance()
        }
    }

    fun toLogin(deepLinkInfo: LoginDeepLinkInfo) {
        activity.addFragmentBackStack("LoginFragment", R.id.fragment_container) {
            LoginFragment.newInstance(deepLinkInfo)
        }
    }

    fun toPreviousView() {
        activity.toPreviousView()
    }

    fun toTwoFA(username: String, password: String) {
        activity.addFragmentBackStack("TwoFAFragment", R.id.fragment_container) {
            TwoFAFragment.newInstance(username, password)
        }
    }

    fun toSignUp() {
        activity.addFragmentBackStack("SignupFragment", R.id.fragment_container) {
            SignupFragment.newInstance()
        }
    }

    fun toForgotPassword() {
        activity.addFragmentBackStack("ResetPasswordFragment", R.id.fragment_container) {
            ResetPasswordFragment.newInstance()
        }
    }

    fun toWebPage(url: String, toolbarTitle: String? = null) {
        activity.startActivity(activity.webViewIntent(url, toolbarTitle))
        activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
    }

    fun toRegisterUsername(userId: String, authToken: String) {
        activity.addFragmentBackStack("RegisterUsernameFragment", R.id.fragment_container) {
            RegisterUsernameFragment.newInstance(userId, authToken)
        }
    }

    fun toCreateAccount() {
        activity.addFragmentBackStack("SignUpFragment", R.id.fragment_container){
            SignupFragment.newInstance()
        }
    }

    fun toChatList() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    fun toChatList(serverUrl: String) {
        activity.startActivity(activity.changeServerIntent(serverUrl))
        activity.finish()
    }

    fun toServerScreen() {
        activity.startActivity(activity.newServerIntent())
        activity.finish()
    }
}