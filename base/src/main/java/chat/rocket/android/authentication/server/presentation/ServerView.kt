package chat.rocket.android.authentication.server.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ServerView : LoadingView, MessageView, VersionCheckView {

    /**
     * Shows an invalid server URL message.
     */
    fun showInvalidServerUrlMessage()
}