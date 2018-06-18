package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.newInstance
import chat.rocket.android.util.addFragment

class ChatRoomNavigator(internal val activity: ChatRoomActivity) {
    fun toChatRooms(chatRoomId: String, chatRoomName: String, chatRoomType: String) {
        activity.addFragment("ChatRoomFragment", R.id.fragment_container) {
            newInstance(chatRoomId, chatRoomName, chatRoomType)
        }
    }
}