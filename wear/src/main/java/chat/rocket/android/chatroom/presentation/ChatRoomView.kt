package chat.rocket.android.chatroom.presentation

import chat.rocket.android.chatroom.models.messages.MessageUiModel
import chat.rocket.android.core.behaviour.LoadingView
import chat.rocket.android.core.behaviour.MessagesView


interface ChatRoomView : MessagesView, LoadingView {

    /**
     * Shows the chat room messages.
     *
     * @param dataSet The data set to show.
     */
    fun showMessages(dataSet: List<MessageUiModel>)
}