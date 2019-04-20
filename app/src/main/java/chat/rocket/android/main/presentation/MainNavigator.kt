package chat.rocket.android.main.presentation

import chat.rocket.android.R
import chat.rocket.android.authentication.ui.newServerIntent
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.chatrooms.ui.TAG_CHAT_ROOMS_FRAGMENT
import chat.rocket.android.createchannel.ui.TAG_CREATE_CHANNEL_FRAGMENT
import chat.rocket.android.directory.ui.TAG_DIRECTORY_FRAGMENT
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.profile.ui.TAG_PROFILE_FRAGMENT
import chat.rocket.android.server.ui.changeServerIntent
import chat.rocket.android.settings.ui.TAG_SETTINGS_FRAGMENT
import chat.rocket.android.util.extensions.addFragment
import chat.rocket.android.util.extensions.addFragmentBackStack
import chat.rocket.android.webview.adminpanel.ui.TAG_ADMIN_PANEL_WEB_VIEW_FRAGMENT
import chat.rocket.android.webview.ui.webViewIntent

class MainNavigator(internal val activity: MainActivity) {

    fun toChatList(chatRoomId: String? = null) {
        activity.addFragment(TAG_CHAT_ROOMS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.chatrooms.ui.newInstance(chatRoomId)
        }
    }

    fun toSettings() {
        activity.addFragmentBackStack(TAG_SETTINGS_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.settings.ui.newInstance()
        }
    }

    fun toDirectory() {
        activity.addFragmentBackStack(TAG_DIRECTORY_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.directory.ui.newInstance()
        }
    }

    fun toCreateChannel() {
        activity.addFragmentBackStack(TAG_CREATE_CHANNEL_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.createchannel.ui.newInstance()
        }
    }

    fun toProfile() {
        activity.addFragmentBackStack(TAG_PROFILE_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.profile.ui.newInstance()
        }
    }

    fun toAdminPanel(webPageUrl: String, userToken: String) {
        activity.addFragmentBackStack(TAG_ADMIN_PANEL_WEB_VIEW_FRAGMENT, R.id.fragment_container) {
            chat.rocket.android.webview.adminpanel.ui.newInstance(webPageUrl, userToken)
        }
    }

    fun toLicense(licenseUrl: String, licenseTitle: String) {
        activity.startActivity(activity.webViewIntent(licenseUrl, licenseTitle))
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