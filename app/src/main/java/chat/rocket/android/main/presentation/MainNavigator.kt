package chat.rocket.android.main.presentation

import chat.rocket.android.R
import chat.rocket.android.authentication.ui.newServerIntent
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.createchannel.ui.CreateChannelFragment
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.profile.ui.ProfileFragment
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.settings.ui.SettingsFragment
import chat.rocket.android.util.extensions.addFragment

class MainNavigator(internal val activity: MainActivity) {

    fun toChatList(chatRoomId: String? = null) {
        activity.addFragment("ChatRoomsFragment", R.id.fragment_container) {
            ChatRoomsFragment.newInstance(chatRoomId)
        }
    }

    fun toCreateChannel() {
        activity.addFragment("CreateChannelFragment", R.id.fragment_container) {
            CreateChannelFragment.newInstance()
        }
    }

    fun toUserProfile() {
        activity.addFragment("ProfileFragment", R.id.fragment_container) {
            ProfileFragment.newInstance()
        }
    }

    fun toSettings() {
        activity.addFragment("SettingsFragment", R.id.fragment_container) {
            SettingsFragment.newInstance()
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

    fun toNewServer(serverUrl: String? = null) {
        activity.startActivity(activity.changeServerIntent(serverUrl = serverUrl))
        activity.finish()
    }

    fun toServerScreen() {
        activity.startActivity(activity.newServerIntent())
    }
}