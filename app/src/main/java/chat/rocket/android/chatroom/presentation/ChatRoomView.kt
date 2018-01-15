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

    /**
     * Send a message to a chat room.
     *
     * @param text The text to send.
     */
    fun sendMessage(text: String)

    /**
     * Shows a (recent) message sent to a chat room.

     * @param message The (recent) message sent to a chat room.
     */
    fun showSentMessage(message: Message)
}