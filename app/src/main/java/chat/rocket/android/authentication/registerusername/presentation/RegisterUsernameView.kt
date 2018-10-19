package chat.rocket.android.authentication.registerusername.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface RegisterUsernameView : LoadingView, MessageView {

    /**
     * Enables the button to set the username if the user entered at least one character.
     */
    fun enableButtonUseThisUsername()

    /**
     * Disables the button to set the username when there is no character entered by the user.
     */
    fun disableButtonUseThisUsername()
}