package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.chatRoomIntent
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
        activity.addFragmentBackStack("PinnedMessages", R.id.fragment_container){
            chat.rocket.android.pinnedmessages.ui.newInstance(chatRoomId,chatRoomType)
        }
    }

    fun toNewServer() {
        activity.startActivity(activity.changeServerIntent())
        activity.finish()
    }

    fun toDirectMessage(chatRoomId: String,
                   chatRoomName: String,
                   chatRoomType: String,
                   isChatRoomReadOnly: Boolean,
                   chatRoomLastSeen: Long,
                   isChatRoomSubscribed: Boolean,
                   isChatRoomOwner: Boolean) {
        activity.startActivity(activity.chatRoomIntent(chatRoomId, chatRoomName, chatRoomType,
            isChatRoomReadOnly, chatRoomLastSeen, isChatRoomSubscribed, isChatRoomOwner))
        activity.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }
}