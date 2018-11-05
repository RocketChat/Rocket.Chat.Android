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

    fun toSignInToYourServer() {
        activity.addFragmentBackStack(ScreenViewEvent.Server.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.server.ui.newInstance()
        }
    }

    fun toLoginOptions(
        serverUrl: String,
        state: String? = null,
        facebookOauthUrl: String? = null,
        githubOauthUrl: String? = null,
        googleOauthUrl: String? = null,
        linkedinOauthUrl: String? = null,
        gitlabOauthUrl: String? = null,
        wordpressOauthUrl: String? = null,
        casLoginUrl: String? = null,
        casToken: String? = null,
        casServiceName: String? = null,
        casServiceNameTextColor: Int = 0,
        casServiceButtonColor: Int = 0,
        customOauthUrl: String? = null,
        customOauthServiceName: String? = null,
        customOauthServiceNameTextColor: Int = 0,
        customOauthServiceButtonColor: Int = 0,
        samlUrl: String? = null,
        samlToken: String? = null,
        samlServiceName: String? = null,
        samlServiceNameTextColor: Int = 0,
        samlServiceButtonColor: Int = 0,
        totalSocialAccountsEnabled: Int = 0,
        isLoginFormEnabled: Boolean = true,
        isNewAccountCreationEnabled: Boolean = true,
        deepLinkInfo: LoginDeepLinkInfo? = null
    ) {
        activity.addFragmentBackStack(
            ScreenViewEvent.LoginOptions.screenName,
            R.id.fragment_container
        ) {
            chat.rocket.android.authentication.loginoptions.ui.newInstance(
                serverUrl,
                state,
                facebookOauthUrl,
                githubOauthUrl,
                googleOauthUrl,
                linkedinOauthUrl,
                gitlabOauthUrl,
                wordpressOauthUrl,
                casLoginUrl,
                casToken,
                casServiceName,
                casServiceNameTextColor,
                casServiceButtonColor,
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
                isNewAccountCreationEnabled,
                deepLinkInfo
            )
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

    fun toLogin(serverUrl: String) {
        activity.addFragmentBackStack(ScreenViewEvent.Login.screenName, R.id.fragment_container) {
            chat.rocket.android.authentication.login.ui.newInstance(serverUrl)
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
