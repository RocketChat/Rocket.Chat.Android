package chat.rocket.android.chatroom.presentation

import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.core.model.Message
import chat.rocket.core.model.Value

interface ChatRoomView : LoadingView, MessageView {

    /**
     * Shows the chat room messages.
     *
     * @param dataSet The data set to show.
     * @param serverUrl The server URL.
     * @param settings The server settings.
     */
    fun showMessages(dataSet: List<Message>, serverUrl: String, settings: Map<String, Value<Any>>?)

    /**
     * Send a message to a chat room.
     *
     * @param text The text to send.
     */
    fun sendMessage(text: String)

    /**
     * Shows a (recent) message sent to a chat room.
     *
     * @param message The (recent) message sent to a chat room.
     */
    fun showNewMessage(message: Message)

    /**
     * Dispatch a update to the recycler views adapter about a changed message.
     *
     * @param index The index of the changed message
     */
    fun dispatchUpdateMessage(index: Int, message: Message)

    fun disableMessageInput()

    fun enableMessageInput(clear: Boolean = false)
}