package chat.rocket.android.chatroom.presentation

import chat.rocket.android.R
import chat.rocket.android.chatdetails.ui.TAG_CHAT_DETAILS_FRAGMENT
import chat.rocket.android.chatinformation.ui.messageInformationIntent
import chat.rocket.android.chatroom.ui.ChatRoomActivity
import chat.rocket.android.chatroom.ui.bottomsheet.WebUrlBottomSheet
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.favoritemessages.ui.TAG_FAVORITE_MESSAGES_FRAGMENT
import chat.rocket.android.files.ui.TAG_FILES_FRAGMENT
import chat.rocket.android.inviteusers.ui.TAG_INVITE_USERS_FRAGMENT
import chat.rocket.android.members.ui.TAG_MEMBERS_FRAGMENT
import chat.rocket.android.mentions.ui.TAG_MENTIONS_FRAGMENT
import chat.rocket.android.pinnedmessages.ui.TAG_PINNED_MESSAGES_FRAGMENT
import chat.rocket.android.profile.ui.TAG_IMAGE_DIALOG_FRAGMENT
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.userdetails.ui.TAG_USER_DETAILS_FRAGMENT
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.videoconference.ui.videoConferenceIntent
import chat.rocket.android.webview.ui.webViewIntent

class ChatRoomNavigator(internal val activity: ChatRoomActivity) {

    fun toUserDetails(userId: String, chatRoomId: String) {
        activity.addFragmentBackStack(TAG_USER_DETAILS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.userdetails.ui.newInstance(userId, chatRoomId)
        }
    }

    fun toVideoConference(chatRoomId: String, chatRoomType: String) {
        activity.startActivity(activity.videoConferenceIntent(chatRoomId, chatRoomType))
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
        isChatRoomFavorite: Boolean,
        isMenuDisabled: Boolean
    ) {
        activity.addFragmentBackStack(TAG_CHAT_DETAILS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.chatdetails.ui.newInstance(
                chatRoomId,
                chatRoomType,
                isChatRoomSubscribed,
                isChatRoomFavorite,
                isMenuDisabled
            )
        }
    }

    fun toMembersList(chatRoomId: String) {
        activity.addFragmentBackStack(TAG_MEMBERS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.members.ui.newInstance(chatRoomId)
        }
    }

    fun toInviteUsers(chatRoomId: String) {
        activity.addFragmentBackStack(TAG_INVITE_USERS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.inviteusers.ui.newInstance(chatRoomId)
        }
    }

    fun toMemberDetails(userId: String, chatRoomId: String) {
        activity.addFragmentBackStack(TAG_USER_DETAILS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.userdetails.ui.newInstance(userId, chatRoomId)
        }
    }

    fun toMentions(chatRoomId: String) {
        activity.addFragmentBackStack(TAG_MENTIONS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.mentions.ui.newInstance(chatRoomId)
        }
    }

    fun toPinnedMessageList(chatRoomId: String) {
        activity.addFragmentBackStack(TAG_PINNED_MESSAGES_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.pinnedmessages.ui.newInstance(chatRoomId)
        }
    }

    fun toFavoriteMessageList(chatRoomId: String) {
        activity.addFragmentBackStack(TAG_FAVORITE_MESSAGES_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.favoritemessages.ui.newInstance(chatRoomId)
        }
    }

    fun toFileList(chatRoomId: String) {
        activity.addFragmentBackStack(TAG_FILES_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.files.ui.newInstance(chatRoomId)
        }
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

    fun toProfileImage(avatarUrl: String) {
        activity.addFragmentBackStack(TAG_IMAGE_DIALOG_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.profile.ui.newInstance(avatarUrl)
        }
    }

    fun toFullWebPage(roomId: String, url: String) {
        activity.startActivity(activity.webViewIntent(url,null))
        activity.overridePendingTransition(R.anim.open_enter, R.anim.open_exit)
    }

    fun toConfigurableWebPage(roomId: String, url: String, heightRatio: String) {
        val weburlbottomsheet = WebUrlBottomSheet.newInstance(url, roomId, heightRatio)
        weburlbottomsheet.show(activity.supportFragmentManager, null)
    }
}
