package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.members.ui.newInstance
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.util.extensions.addFragmentBackStack

class ChatRoomNavigator(internal val activity: ChatRoomActivity) {

    fun toMembersList(chatRoomId: String, chatRoomType: String) {
        activity.addFragmentBackStack("MembersFragment", R.id.fragment_container) {
            newInstance(chatRoomId, chatRoomType)
        }
    }

    fun toPinnedMessageList(chatRoomId: String, chatRoomType: String) {
        activity.addFragmentBackStack("PinnedMessages", R.id.fragment_container) {
            chat.rocket.android.pinnedmessages.ui.newInstance(chatRoomId, chatRoomType)
        }
    }

    fun toFavoriteMessageList(chatRoomId: String, chatRoomType: String) {
        activity.addFragmentBackStack("FavoriteMessages", R.id.fragment_container) {
            chat.rocket.android.favoritemessages.ui.newInstance(chatRoomId, chatRoomType)
        }
    }

    fun toNewServer() {
        activity.startActivity(activity.changeServerIntent())
        activity.finish()
    }
}