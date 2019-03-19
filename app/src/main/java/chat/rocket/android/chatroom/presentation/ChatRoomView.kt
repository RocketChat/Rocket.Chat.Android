package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.uimodel.BaseUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.ChatRoomSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.CommandSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.EmojiSuggestionUiModel
import chat.rocket.android.chatroom.uimodel.suggestion.PeopleSuggestionUiModel
import chat.rocket.android.chatrooms.adapter.model.RoomUiModel
import chat.rocket.android.core.behaviours.LoadingView
import chat.rocket.android.core.behaviours.MessageView
import chat.rocket.core.internal.realtime.socket.model.State
import chat.rocket.core.model.ChatRoom

interface ChatRoomView : LoadingView, MessageView {

    /**
     * Shows the chat room messages.
     *
     * @param dataSet The data set to show.
     * @param clearDataSet If true it will clear the previous data set.
     */
    fun showMessages(dataSet: List<BaseUiModel<*>>, clearDataSet: Boolean)

    /**
     * Shows the chat room messages in the basis of a search term.
     *
     * @param dataSet The data set to show.
     */
    fun showSearchedMessages(dataSet: List<BaseUiModel<*>>)

    /**
     * Send a message to a chat room.
     *
     * @param text The text to send.
     */
    fun sendMessage(text: String)

    /**
     * Shows the username(s) of the user(s) who is/are typing in the chat room.
     *
     * @param usernameList The list of username to show.
     */
    fun showTypingStatus(usernameList: List<String>)

    /**
     * Hides the typing status view.
     */
    fun hideTypingStatusView()

    /**
     * Perform file selection with the mime type [filter]
     */
    fun showFileSelection(filter: Array<String>?)

    /**
     * Shows a invalid file message.
     */
    fun showInvalidFileMessage()

    /**
     * Shows a (recent) message sent to a chat room.
     *
     * @param message The (recent) message sent to a chat room.
     */
    fun showNewMessage(message: List<BaseUiModel<*>>, isMessageReceived: Boolean)

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
    fun dispatchUpdateMessage(index: Int, message: List<BaseUiModel<*>>)

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
    fun clearMessageComposition(deleteMessage: Boolean)

    fun showInvalidFileSize(fileSize: Int, maxFileSize: Int)

    fun showConnectionState(state: State)

    fun populatePeopleSuggestions(members: List<PeopleSuggestionUiModel>)

    fun populateRoomSuggestions(chatRooms: List<ChatRoomSuggestionUiModel>)

    fun populateEmojiSuggestions(emojis: List<EmojiSuggestionUiModel>)

    fun onJoined(roomUiModel: RoomUiModel)

    fun showReactionsPopup(messageId: String)

    /**
     * Show list of commands.
     *
     * @param commands The list of available commands.
     */
    fun populateCommandSuggestions(commands: List<CommandSuggestionUiModel>)

    fun onRoomUpdated(roomUiModel: RoomUiModel)

}
