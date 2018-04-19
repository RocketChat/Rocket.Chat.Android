package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.util.extensions.addFragmentBackStack

class ChatRoomNavigator(internal val activity: ChatRoomActivity) {

    fun toMembersList(chatRoomId: String, chatRoomType: String) {
        activity.addFragmentBackStack("MembersFragment", R.id.fragment_container) {
            chat.rocket.android.members.ui.newInstance(chatRoomId, chatRoomType)
        }
    }

    fun toChatInfo(chatRoomId: String, chatRoomType: String, isSubscribed: Boolean) {
        activity.addFragmentBackStack("InformationFragment", R.id.fragment_container) {
            chat.rocket.android.chatroom.information.ui.newInstance(chatRoomId, chatRoomType, isSubscribed)
        }
    }

    fun toEditChatInfo(chatRoomId: String, chatRoomType: String) {
        activity.addFragmentBackStack("EditInfoFragment", R.id.fragment_container) {
            chat.rocket.android.chatroom.edit.ui.newInstance(chatRoomId, chatRoomType)
        }
    }

    fun toNewServer() {
        activity.startActivity(activity.changeServerIntent())
        activity.finish()
    }
}