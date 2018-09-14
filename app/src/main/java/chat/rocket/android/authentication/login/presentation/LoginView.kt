package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface LoginView : LoadingView, MessageView {

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
     * REMARK: We must set up the CAS button listener before showing it [setupCasButtonListener].
     */
    fun showCasButton()

    /**
     * Hides the CAS button.
     */
    fun hideCasButton()

    /**
     * Setups the CAS button when tapped.
     *
     * @param casUrl The CAS URL to authenticate with.
     * @param casToken The requested token to be sent to the CAS server.
     */
    fun setupCasButtonListener(casUrl: String, casToken: String)

    /**
     * Shows the forgot password view if enabled by the server settings.
     *
     * REMARK: We must set up the forgot password view listener [setupForgotPasswordView].
     */
    fun showForgotPasswordView()

    /**
     * Setups the forgot password view when tapped.
     */
    fun setupForgotPasswordView()

    /**
     * Adds a custom OAuth button in the oauth view.
     *
     * @customOauthUrl The custom OAuth url to sets up the button (the listener).
     * @state A random string generated by the app, which you'll verify later (to protect against forgery attacks).
     * @serviceName The custom OAuth service name.
     * @serviceNameColor The custom OAuth service name color (just stylizing).
     * @buttonColor The custom OAuth button color (just stylizing).
     * @see [enableOauthView]
     */
    fun addCustomOauthServiceButton(
        customOauthUrl: String,
        state: String,
        serviceName: String,
        serviceNameColor: Int,
        buttonColor: Int
    )

    /**
     *  Adds a SAML button in the oauth view.
     *
     * @samlUrl The SAML url to sets up the button (the listener).
     * @serviceName The SAML service name.
     * @serviceNameColor The SAML service name color (just stylizing).
     * @buttonColor The SAML button color (just stylizing).
     * @see [enableOauthView]
     */
    fun addSamlServiceButton(
        samlUrl: String,
        samlToken: String,
        serviceName: String,
        serviceNameColor: Int,
        buttonColor: Int
    )

    /**
     * Alerts the user about a wrong inputted username or email.
     */
    fun alertWrongUsernameOrEmail()

    /**
     * Alerts the user about a wrong inputted password.
     */
    fun alertWrongPassword()

    /**
     * Saves Google Smart Lock credentials.
     */
    fun saveSmartLockCredentials(id: String, password: String)
}