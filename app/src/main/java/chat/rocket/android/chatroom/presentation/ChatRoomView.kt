package chat.rocket.android.chatroom.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.core.model.Message

interface ChatRoomView : LoadingView, MessageView {

    /**
     * Shows the chat room messages.
     *
     * @param dataSet The data set to show.
     * @param serverUrl The server URL.
     */
    fun showMessages(dataSet: MutableList<Message>, serverUrl: String)
}