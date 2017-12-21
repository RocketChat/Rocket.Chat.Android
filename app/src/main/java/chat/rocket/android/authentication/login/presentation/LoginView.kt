package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.android.core.behaviours.ShakeView

interface LoginView : LoadingView, MessageView, ShakeView {

    /**
     * Shows the oauth view if the server settings allow the login via social accounts.
     *
     * REMARK: we must show at maximum *three* social accounts views ([enableLoginByFacebook], [enableLoginByGithub], [enableLoginByGoogle],
     * [enableLoginByLinkedin], [enableLoginByMeteor], [enableLoginByTwitter] or [enableLoginByGitlab]) for the oauth view.
     * If the possibility of login via social accounts exceeds 3 different ways we should set up the FAB ([setupFabListener]) to show the remaining view(s).
     *
     * @param show True to show the oauth view, false otherwise.
     */
    fun shouldShowOauthView(show: Boolean)

    /**
     * Setups the FloatingActionButton to show more social accounts views (expanding the oauth view interface to show the remaining view(s)).
     */
    fun setupFabListener()

    /**
     * Shows the login by Facebook view.
     */
    fun enableLoginByFacebook()

    /**
     * Shows the login by Github view.
     */
    fun enableLoginByGithub()

    /**
     * Shows the login by Google view.
     */
    fun enableLoginByGoogle()

    /**
     * Shows the login by Linkedin view.
     */
    fun enableLoginByLinkedin()

    /**
     * Shows the login by Meteor view.
     */
    fun enableLoginByMeteor()

    /**
     * Shows the login by Twitter view.
     */
    fun enableLoginByTwitter()

    /**
     * Shows the login by Gitlab view.
     */
    fun enableLoginByGitlab()

    /**
     * Shows the sign up view if the server settings allow the new users registration.
     *
     * @param show True to show the sign up view, false otherwise.
     */
    fun shouldShowSignUpView(show: Boolean)
}