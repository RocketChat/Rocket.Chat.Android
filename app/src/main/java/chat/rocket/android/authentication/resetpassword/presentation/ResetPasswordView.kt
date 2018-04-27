package chat.rocket.android.authentication.resetpassword.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ResetPasswordView : LoadingView, MessageView {

    /**
     * Alerts the user about a blank email.
     */
    fun alertBlankEmail()

    /**
     * Alerts the user about a invalid email.
     */
    fun alertInvalidEmail()

    /**
     * Shows a successful email sent message.
     */
    fun emailSent()
}