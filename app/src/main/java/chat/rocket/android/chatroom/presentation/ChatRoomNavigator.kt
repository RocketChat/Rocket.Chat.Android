package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatroom.information.ui.newInstance
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.members.ui.newInstance
import chat.rocket.android.util.extensions.addFragmentBackStack

class ChatRoomNavigator(internal val activity: ChatRoomActivity) {

    fun toMembersList(chatRoomId: String, chatRoomType: String) {
        activity.addFragmentBackStack("MembersFragment", R.id.fragment_container) {
            newInstance(chatRoomId, chatRoomType)
        }
    }

    fun toChatInfo(chatRoomId: String) {
        activity.addFragmentBackStack("InformationFragment", R.id.fragment_container) {
            newInstance(chatRoomId)
        }
    }

    fun toMutedUsersList(chatRoomId: String) {
        activity.addFragmentBackStack("MutedUsersFragment", R.id.fragment_container) {
            newInstance(chatRoomId)
        }
    }
}