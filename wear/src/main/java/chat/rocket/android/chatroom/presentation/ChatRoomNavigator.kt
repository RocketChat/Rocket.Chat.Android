package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatroom.models.messages.MessageUiModel
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.newInstance
import chat.rocket.android.util.addFragmentBackStack

class ChatRoomNavigator(internal val activity: ChatRoomActivity) {
    fun toChatRoom(chatRoomId: String, chatRoomName: String, chatRoomType: String) {
        activity.addFragmentBackStack("ChatRoomFragment", R.id.fragment_container) {
            newInstance(chatRoomId, chatRoomName, chatRoomType)
        }
    }

    fun toCompleteMessage(message: MessageUiModel) {
        activity.addFragmentBackStack("CompleteChatRoomFragment", R.id.fragment_container) {
            newInstance(message)
        }
    }
}