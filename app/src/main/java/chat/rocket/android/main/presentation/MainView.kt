package chat.rocket.android.main.presentation

import chat.rocket.android.core.behaviours.MessageView

interface MainView : MessageView {

    /**
     * User has successfully logged out from the current server.
     **/
    fun onLogout()
}