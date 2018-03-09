package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.core.behaviours.InternetView
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface LoginView : LoadingView, MessageView, InternetView {

    /**
     * Shows the CAS view if the server settings allow the sign in/sign out via CAS protocol.

     * @param casUrl The CAS URL to login/sign up with.
     * @param requestedToken The requested Token sent to the CAS server.
     */
    fun showCasView(casUrl: String, requestedToken: String)

    /**
     * Shows the sign up view if the server settings allow the new users registration.
     */
    fun showSignUpView()

    /**
     * Shows the oauth view if the server settings allow the login via social accounts.
     *
     * REMARK: we must show at maximum *three* social accounts views ([enableLoginByFacebook], [enableLoginByGithub], [enableLoginByGoogle],
     * [enableLoginByLinkedin], [enableLoginByMeteor], [enableLoginByTwitter] or [enableLoginByGitlab]) for the oauth view.
     * If the possibility of login via social accounts exceeds 3 different ways we should set up the FAB ([setupFabListener]) to show the remaining view(s).
     */
    fun showOauthView()

    /**
     * Shows the login button.
     */
    fun showLoginButton()

    /**
     * Shows the login by Facebook view if the server settings allow it.
     */
    fun enableLoginByFacebook()

    /**
     * Shows the login by Github view if the server settings allow it.
     */
    fun enableLoginByGithub()

    /**
     * Shows the login by Google view if the server settings allow it.
     */
    fun enableLoginByGoogle()

    /**
     * Shows the login by Linkedin view if the server settings allow it.
     */
    fun enableLoginByLinkedin()

    /**
     * Shows the login by Meteor view if the server settings allow it.
     */
    fun enableLoginByMeteor()

    /**
     * Shows the login by Twitter view if the server settings allow it.
     */
    fun enableLoginByTwitter()

    /**
     * Shows the login by Gitlab view if the server settings allow it.
     */
    fun enableLoginByGitlab()

    /**
     * Setups the FloatingActionButton to show more social accounts views (expanding the oauth view interface to show the remaining view(s)).
     */
    fun setupFabListener()

    /**
     * Hides the username/e-mail view.
     */
    fun hideUsernameOrEmailView()

    /**
     * Hides the password view.
     */
    fun hidePasswordView()

    /**
     * Hides the sign up view if the server settings does not allow the new users registration.
     */
    fun hideSignUpView()

    /**
     * Hides the oauth view.
     */
    fun hideOauthView()

    /**
     * Hides the login button.
     */
    fun hideLoginButton()
    /**
     * Alerts the user about a wrong inputted username or email.
     */
    fun alertWrongUsernameOrEmail()

    /**
     * Alerts the user about a wrong inputted password.
     */
    fun alertWrongPassword()
}