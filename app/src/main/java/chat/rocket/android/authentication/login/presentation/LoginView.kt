package chat.rocket.android.authentication.login.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface LoginView : LoadingView, MessageView {

    /**
     * Shows the forgot password view if enabled by the server settings.
     */
    fun showForgotPasswordView()

    /**
     * Saves Google Smart Lock credentials.
     */
    fun saveSmartLockCredentials(id: String, password: String)

    /**
     * Enables the button to login when the user inputs an username and a password.
     */
    fun enableButtonLogin()

    /**
     * Disables the button to login when there is not an entered/not blank username or password by
     * the user (i.e. The fields are empty/blank)
     */
    fun disableButtonLogin()

    /**
     * Enables the forget password button after requesting a processing to log in.
     */
    fun enableButtonForgetPassword()

    /**
     * Disables the forget password button when requesting a processing to log in.
     */
    fun disableButtonForgetPassword()
}
