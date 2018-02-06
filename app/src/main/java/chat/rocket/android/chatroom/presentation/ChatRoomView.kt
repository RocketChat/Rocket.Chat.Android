package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ChatRoomView : LoadingView, MessageView {

    /**
     * Shows the chat room messages.
     *
     * @param dataSet The data set to show.
     */
    fun showMessages(dataSet: List<MessageViewModel>)

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
     * Dispatch a update to the recycler views adapter about a changed message.
     *
     * @param index The index of the changed message
     */
    fun dispatchUpdateMessage(index: Int, message: MessageViewModel)

    fun disableMessageInput()

    fun enableMessageInput(clear: Boolean = false)
}