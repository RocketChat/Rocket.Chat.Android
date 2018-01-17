package chat.rocket.android.chatrooms.presentation

import android.content.Context
import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.chatrooms.ui.ChatRoomsActivity

class ChatRoomsNavigator(private val activity: ChatRoomsActivity, private val context: Context) {

    fun toChatRoom(chatRoomId: String, chatRoomName: String, chatRoomType: String, isChatRoomReadOnly: Boolean) {
        activity.startActivity(context.chatRoomIntent(chatRoomId, chatRoomName, chatRoomType, isChatRoomReadOnly))
        activity.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }
}