package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ChatRoomView : LoadingView, MessageView {

    /**
     * Shows the chat room messages.
     *
     * @param dataSet The data set to show.
     * @param serverUrl The server URL.
     */
    fun showMessages(dataSet: List<MessageViewModel>, serverUrl: String)

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
    fun showNewMessage(message: MessageViewModel)

    /**
     * Dispatch to the recycler views adapter that we should remove a message.
     *
     * @param msgId The id of the message to be removed.
     */
    fun dispatchDeleteMessage(msgId: String)

    /**
     * Dispatch a update to the recycler views adapter about a changed message.
     *
     * @param index The index of the changed message
     */
    fun dispatchUpdateMessage(index: Int, message: MessageViewModel)

    fun disableMessageInput()
    fun enableMessageInput(clear: Boolean = false)
}