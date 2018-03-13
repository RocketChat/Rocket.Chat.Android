package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.core.behaviours.InternetView
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface LoginView : LoadingView, MessageView, InternetView {

    /**
     * Shows the form view (i.e the username/email and password fields) if it is enabled by the server settings.
     *
     * REMARK: We must set up the login button listener [setupLoginButtonListener].
     * Remember to enable [enableUserInput] or disable [disableUserInput] the view interaction for the user when submitting the form.
     */
    fun showFormView()

    /**
     * Hides the form view.
     */
    fun hideFormView()

    /**
     * Setups the login button when tapped.
     */
    fun setupLoginButtonListener()

    /**
     * Enables the view interactions for the user.
     */
    fun enableUserInput()

    /**
     * Disables the view interactions for the user.
     */
    fun disableUserInput()

    /**
     * Shows the CAS button if the sign in/sign out via CAS protocol is enabled by the server settings.
     *
     * REMARK: We must set up the CAS button listener [setupCasButtonListener].
     */
    fun showCasButton()

    /**
     * Hides the CAS button.
     */
    fun hideCasButton()

    /**
     * Setups the CAS button when tapped.
     *
     * @param casUrl The CAS URL to login/sign up with.
     * @param casToken The requested Token sent to the CAS server.
     */
    fun setupCasButtonListener(casUrl: String, casToken: String)

    /**
     * Shows the sign up view if the new users registration is enabled by the server settings.
     *
     * REMARK: We must set up the sign up view listener [setupSignUpView].
     */
    fun showSignUpView()

    /**
     * Setups the sign up view when tapped.
     */
    fun setupSignUpView()

    /**
     * Hides the sign up view.
     */
    fun hideSignUpView()

    /**
     * Shows the oauth view if the login via social accounts is enabled by the server settings.
     *
     * REMARK: We must show at maximum *three* social accounts views ([enableLoginByFacebook], [enableLoginByGithub], [enableLoginByGoogle],
     * [enableLoginByLinkedin], [enableLoginByMeteor], [enableLoginByTwitter] or [enableLoginByGitlab]) for the oauth view.
     * If the possibility of login via social accounts exceeds 3 different ways we should set up the FAB ([setupFabListener]) to show the remaining view(s).
     */
    fun showOauthView()

    /**
     * Hides the oauth view.
     */
    fun hideOauthView()

    /**
     * Shows the login button.
     */
    fun showLoginButton()

    /**
     * Hides the login button.
     */
    fun hideLoginButton()

    /**
     * Shows the "login by Facebook view if it is enabled by the server settings.
     */
    fun enableLoginByFacebook()

    /**
     * Shows the "login by Github" view if it is enabled by the server settings.
     */
    fun enableLoginByGithub()

    /**
     * Shows the "login by Google" view if it is enabled by the server settings.
     */
    fun enableLoginByGoogle()

    /**
     * Shows the "login by Linkedin" view if it is enabled by the server settings.
     */
    fun enableLoginByLinkedin()

    /**
     * Shows the "login by Meteor" view if it is enabled by the server settings.
     */
    fun enableLoginByMeteor()

    /**
     * Shows the "login by Twitter" view if it is enabled by the server settings.
     */
    fun enableLoginByTwitter()

    /**
     * Shows the "login by Gitlab" view if it is enabled by the server settings.
     */
    fun enableLoginByGitlab()

    /**
     * Setups the FloatingActionButton to show more social accounts views (expanding the oauth view interface to show the remaining view(s)).
     */
    fun setupFabListener()

    fun setupGlobalListener()

    /**
     * Alerts the user about a wrong inputted username or email.
     */
    fun alertWrongUsernameOrEmail()

    /**
     * Alerts the user about a wrong inputted password.
     */
    fun alertWrongPassword()
}