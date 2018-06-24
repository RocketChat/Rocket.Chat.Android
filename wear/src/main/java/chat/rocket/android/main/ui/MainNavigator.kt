package chat.rocket.android.main.ui

import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.util.addFragmentBackStack

class MainNavigator(internal val activity: MainActivity) {
    fun toChatRoom(
        chatRoomId: String,
        chatRoomName: String,
        chatRoomType: String
    ) {
        activity.startActivity(activity.chatRoomIntent(chatRoomId, chatRoomName, chatRoomType))
    }

    fun addChatRoomsFragment(chatRoomsFragment: ChatRoomsFragment){
        activity.addFragmentBackStack("ChatRoomsFragment", R.id.content_frame){
            chatRoomsFragment
        }
    }
}