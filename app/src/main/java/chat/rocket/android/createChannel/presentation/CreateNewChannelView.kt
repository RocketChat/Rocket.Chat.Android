package chat.rocket.android.createChannel.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface CreateNewChannelView : LoadingView, MessageView {
    /*
    Show a message that a channel was successfully created
    */
    fun showChannelCreatedSuccessfullyMessage()

}