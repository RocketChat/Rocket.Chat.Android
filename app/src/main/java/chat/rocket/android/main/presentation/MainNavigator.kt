package chat.rocket.android.main.presentation

import chat.rocket.android.R
import chat.rocket.android.authentication.ui.newServerIntent
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.chatrooms.ui.TAG_CHAT_ROOMS_FRAGMENT
import chat.rocket.android.createchannel.ui.CreateChannelFragment
import chat.rocket.android.createchannel.ui.TAG_CREATE_CHANNEL_FRAGMENT
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.profile.ui.ProfileFragment
import chat.rocket.android.profile.ui.TAG_PROFILE_FRAGMENT
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.settings.ui.SettingsFragment
import chat.rocket.android.settings.ui.TAG_SETTINGS_FRAGMENT
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.webview.adminpanel.ui.AdminPanelWebViewFragment

class MainNavigator(internal val activity: MainActivity) {

    fun toChatList(chatRoomId: String? = null) {
        activity.addFragment(TAG_CHAT_ROOMS_FRAGMENT, R.id.fragment_container) {
            ChatRoomsFragment.newInstance(chatRoomId)
        }
    }

    fun toCreateChannel() {
        activity.addFragment(TAG_CREATE_CHANNEL_FRAGMENT, R.id.fragment_container) {
            CreateChannelFragment.newInstance()
        }
    }

    fun toUserProfile() {
        activity.addFragment(TAG_PROFILE_FRAGMENT, R.id.fragment_container) {
            ProfileFragment.newInstance()
        }
    }

    fun toSettings() {
        activity.addFragment(TAG_SETTINGS_FRAGMENT, R.id.fragment_container) {
            SettingsFragment.newInstance()
        }
    }

    fun toAdminPanel(webPageUrl: String, userToken: String) {
        activity.addFragment("AdminPanelWebViewFragment", R.id.fragment_container) {
            AdminPanelWebViewFragment.newInstance(webPageUrl, userToken)
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

    /**
     * Switches to a server, given a [serverUrl] or adds a new server (navigating to the
     * AuthenticationActivity) if the user server list only contains one server and the
     * user logs out from this server.
     * NOTE: If the user has more than one server and logs out from the current server, then it will
     * switch to the first server in the server list.
     *
     * @param serverUrl The server URL to switch from, or null in case user logs out from the
     * current server.
     */
    fun switchOrAddNewServer(serverUrl: String? = null) {
        activity.startActivity(activity.changeServerIntent(serverUrl = serverUrl))
        activity.finish()
    }

    fun toServerScreen() {
        activity.startActivity(activity.newServerIntent())
    }
}