package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatdetails.ui.chatDetailsIntent
import chat.rocket.android.chatinformation.ui.messageInformationIntent
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.favoritemessages.ui.TAG_FAVORITE_MESSAGES_FRAGMENT
import chat.rocket.android.files.ui.TAG_FILES_FRAGMENT
import chat.rocket.android.members.ui.TAG_MEMBERS_FRAGMENT
import chat.rocket.android.mentions.ui.TAG_MENTIONS_FRAGMENT
import chat.rocket.android.pinnedmessages.ui.TAG_PINNED_MESSAGES_FRAGMENT
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.util.extensions.addFragmentBackStack

class ChatRoomNavigator(internal val activity: ChatRoomActivity) {
    fun toChatDetails(
        chatRoomId: String,
        chatRoomType: String,
        isChatRoomSubscribed: Boolean,
        isMenuDisabled: Boolean
    ) {
        activity.startActivity(
            activity.chatDetailsIntent(
                chatRoomId,
                chatRoomType,
                isChatRoomSubscribed,
                isMenuDisabled
            )
        )
    }

    fun toNewServer() {
        activity.startActivity(activity.changeServerIntent())
        activity.finish()
    }

    fun toDirectMessage(
        chatRoomId: String,
        chatRoomName: String,
        chatRoomType: String,
        isChatRoomReadOnly: Boolean,
        chatRoomLastSeen: Long,
        isChatRoomSubscribed: Boolean,
        isChatRoomCreator: Boolean,
        isChatRoomFavorite: Boolean,
        chatRoomMessage: String
    ) {
        activity.startActivity(
            activity.chatRoomIntent(
                chatRoomId,
                chatRoomName,
                chatRoomType,
                isChatRoomReadOnly,
                chatRoomLastSeen,
                isChatRoomSubscribed,
                isChatRoomCreator,
                isChatRoomFavorite,
                chatRoomMessage
            )
        )
        activity.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }

    fun toMessageInformation(messageId: String) {
        activity.startActivity(activity.messageInformationIntent(messageId = messageId))
        activity.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }
}