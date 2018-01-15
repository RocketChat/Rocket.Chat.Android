package chat.rocket.android.authentication.server.presentation

import chat.rocket.android.core.behaviours.InternetView
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ServerView : LoadingView, MessageView, InternetView {

    /**
     * Notifies the user about an invalid inputted server URL.
     */
    fun showInvalidServerUrl()
}