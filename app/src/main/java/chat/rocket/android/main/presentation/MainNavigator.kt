package chat.rocket.android.main.presentation

import android.content.Context
import chat.rocket.android.R
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.profile.ui.ProfileFragment
import chat.rocket.android.settings.ui.SettingsFragment
import chat.rocket.android.util.extensions.addFragment

class MainNavigator(internal val activity: MainActivity, internal val context: Context) {

    fun toChatList() {
        activity.addFragment("ChatRoomsFragment", R.id.fragment_container) {
            ChatRoomsFragment.newInstance()
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
}