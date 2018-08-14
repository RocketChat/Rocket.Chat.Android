package chat.rocket.android.authentication.signup.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface SignupView : LoadingView, MessageView {

    /**
     * Alerts the user about a blank name.
     */
    fun alertBlankName()

    /**
     * Alerts the user about a blank username.
     */
    fun alertBlankUsername()

    /**
     * Alerts the user about a empty password.
     */
    fun alertEmptyPassword()

    /**
     * Alerts the user about a blank email.
     */
    fun alertBlankEmail()

    /**
     * Saves Google Smart Lock credentials.
     */
    fun saveSmartLockCredentials(id: String, password: String)
}