package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatdetails.ui.chatDetailsIntent
import chat.rocket.android.chatinformation.ui.messageInformationIntent
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.userdetails.ui.TAG_USER_DETAILS_FRAGMENT
import chat.rocket.android.util.extensions.addFragmentBackStack
import javax.inject.Inject

class ChatRoomNavigator @Inject constructor(internal val activity: ChatRoomActivity) {

    fun toUserDetails(userId: String) {
        activity.addFragmentBackStack(TAG_USER_DETAILS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.userdetails.ui.newInstance(userId)
        }
    }

    fun toChatRoom(
        chatRoomId: String,
        chatRoomName: String,
        chatRoomType: String,
        isReadOnly: Boolean,
        chatRoomLastSeen: Long,
        isSubscribed: Boolean,
        isCreator: Boolean,
        isFavorite: Boolean
    ) {
        activity.startActivity(
            activity.chatRoomIntent(
                chatRoomId,
                chatRoomName,
                chatRoomType,
                isReadOnly,
                chatRoomLastSeen,
                isSubscribed,
                isCreator,
                isFavorite
            )
        )
        activity.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }

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
