package chat.rocket.android.chatroom.presentation

import android.net.Uri
import chat.rocket.android.chatroom.viewmodel.BaseViewModel
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface ChatRoomView : LoadingView, MessageView {

    /**
     * Shows the chat room messages.
     *
     * @param dataSet The data set to show.
     */
    fun showMessages(dataSet: List<BaseViewModel<*>>)

    /**
     * Send a message to a chat room.
     *
     * @param text The text to send.
     */
    fun sendMessage(text: String)

    /**
     * Perform file selection with the mime type [filter]
     */
    fun showFileSelection(filter: Array<String>)

    /**
     * Uploads a file to a chat room.
     *
     * @param uri The file URI to send.
     */
    fun uploadFile(uri: Uri)

    /**
     * Shows a invalid file message.
     */
    fun showInvalidFileMessage()

    /**
     * Shows a (recent) message sent to a chat room.
     *
     * @param message The (recent) message sent to a chat room.
     */
    fun showNewMessage(message: List<BaseViewModel<*>>)

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
    fun dispatchUpdateMessage(index: Int, message: List<BaseViewModel<*>>)

    /**
     * Show reply status above the message composer.
     *
     * @param username The username or name of the user to reply/quote to.
     * @param replyMarkdown The markdown of the message reply.
     * @param quotedMessage The message to quote.
     */
    fun showReplyingAction(username: String, replyMarkdown: String, quotedMessage: String)

    /**
     * Copy message to clipboard.
     *
     * @param message The message to copy.
     */
    fun copyToClipboard(message: String)

    /**
     * Show edit status above the message composer.
     */
    fun showEditingAction(roomId: String, messageId: String, text: String)

    /**
     * Disabling the send message button avoids the user tap this button multiple
     * times to send a same message.
     */
    fun disableSendMessageButton()

    /**
     * Enables the send message button.
     */
    fun enableSendMessageButton()

    /**
     * Clears the message composition.
     */
    fun clearMessageComposition()

    fun showInvalidFileSize(fileSize: Int, maxFileSize: Int)
}