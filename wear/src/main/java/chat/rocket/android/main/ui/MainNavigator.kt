package chat.rocket.android.main.ui

import android.content.Intent
import chat.rocket.android.R
import chat.rocket.android.account.ui.AccountFragment
import chat.rocket.android.chatroom.ui.chatRoomIntent
import chat.rocket.android.chatrooms.ui.ChatRoomsFragment
import chat.rocket.android.main.settings.ui.SettingsFragment.Companion.newInstance
import chat.rocket.android.starter.ui.StarterActivity
import chat.rocket.android.util.addFragment
import chat.rocket.android.util.addFragmentBackStack

class MainNavigator(internal val activity: MainActivity) {
    fun toChatRoom(
        chatRoomId: String,
        chatRoomName: String,
        chatRoomType: String
    ) {
        activity.startActivity(activity.chatRoomIntent(chatRoomId, chatRoomName, chatRoomType))
    }

    fun addChatRoomsFragment() {
        activity.addFragment("ChatRoomsFragment", R.id.content_frame) {
            ChatRoomsFragment.newInstance()
        }
    }

    fun addSettingsFragment() {
        activity.addFragment("SettingsFragment", R.id.content_frame) {
            newInstance()
        }
    }

    fun addAccountFragment() {
        activity.addFragmentBackStack("AccountFragment", R.id.content_frame) {
            AccountFragment.newInstance()
        }
    }

    fun toStarterActivity() {
        activity.startActivity(Intent(activity, StarterActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        activity.finish()
    }
}