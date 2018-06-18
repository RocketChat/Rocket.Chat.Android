package chat.rocket.android.main.ui

import chat.rocket.android.chatroom.ui.chatRoomIntent

class MainNavigator(internal val activity: MainActivity) {
    fun toChatRoom(
        chatRoomId: String,
        chatRoomName: String,
        chatRoomType: String
    ) {
        activity.startActivity(activity.chatRoomIntent(chatRoomId, chatRoomName, chatRoomType))
    }
}