package chat.rocket.android.authentication.registerusername.presentation

import chat.rocket.android.core.behaviours.InternetView
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface RegisterUsernameView : LoadingView, MessageView, InternetView {

    /**
     * Alerts the user about a blank username.
     */
    fun alertBlankUsername()
}