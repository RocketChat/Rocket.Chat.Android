package chat.rocket.android.authentication.resetpassword.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ResetPasswordView : LoadingView, MessageView {

    /**
     * Shows a successful email sent message.
     */
    fun emailSent()

    /**
     * Shows a message to update the server version in order to use an app feature.
     */
    fun updateYourServerVersion()

    /**
     * Enables the button to reset the password when the user inputs a valid email address.
     */
    fun enableButtonConnect()

    /**
     * Disables the button to reset the password when the user entered an invalid email address
     */
    fun disableButtonConnect()
}