package chat.rocket.android.authentication.signup.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface SignupView : LoadingView, MessageView {

    /**
     * Enables the button to register when the user enters all the required fields.
     */
    fun enableButtonRegister()

    /**
     * Disables the button to register when the user doesn't enter all the required fields.
     */
    fun disableButtonRegister()


    /**
     * Saves Google Smart Lock credentials.
     */
    fun saveSmartLockCredentials(id: String, password: String)
}