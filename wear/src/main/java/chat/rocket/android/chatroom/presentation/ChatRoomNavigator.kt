package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatroom.models.messages.MessageUiModel
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.newInstance
import chat.rocket.android.util.addFragmentBackStack
import chat.rocket.android.util.removeFragmentBackStack

class ChatRoomNavigator(internal val activity: ChatRoomActivity) {
    fun toChatRoom(
        chatRoomId: String,
        chatRoomName: String,
        chatRoomType: String
    ) {
        activity.addFragmentBackStack("ChatRoomFragment", R.id.fragment_container) {
            newInstance(chatRoomId, chatRoomName, chatRoomType)
        }
    }

    fun toCompleteMessage(message: MessageUiModel) {
        activity.addFragmentBackStack("CompleteChatRoomFragment", R.id.fragment_container) {
            newInstance(message)
        }
    }

    fun toReplyMessage(chatRoomId: String, replyText: String = "") {
        activity.addFragmentBackStack("ReplyMessage", R.id.fragment_container) {
            chat.rocket.android.chatroom.reply.ui.newInstance(chatRoomId, replyText)
        }
    }

    fun removeReplyMessageFragment() {
        activity.removeFragmentBackStack("ReplyMessage")
    }
}