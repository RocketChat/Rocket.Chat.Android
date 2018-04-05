package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.viewmodel.BaseViewModel
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView

interface PinnedMessagesView : MessageView, LoadingView {

    /**
     * Show list of pinned messages for the current room.
     *
     * @param pinnedMessages The list of pinned messages.
     */
    fun showPinnedMessages(pinnedMessages: List<BaseViewModel<*>>)

    /**
     * Copy message to clipboard.
     *
     * @param message The message to copy.
     */
    fun copyToClipboard(message: String)
    /**
     * Dispatch to the recycler views adapter that we should remove a message.
     *
     * @param msgId The id of the message to be removed.
     */
    fun dispatchDeleteMessage(msgId: String)
    fun showReactionsPopup(messageId: String)

    /**
     * Show edit status above the message composer.
     */
    fun showEditingAction(roomId: String, messageId: String, text: String)
    /**
     * Show reply status above the message composer.
     *
     * @param username The username or name of the user to reply/quote to.
     * @param replyMarkdown The markdown of the message reply.
     * @param quotedMessage The message to quote.
     */
    fun showReplyingAction(username: String, replyMarkdown: String, quotedMessage: String)
}