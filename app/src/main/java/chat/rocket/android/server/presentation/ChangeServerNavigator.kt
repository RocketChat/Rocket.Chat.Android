package chat.rocket.android.server.presentation

import android.content.Intent
import chat.rocket.android.authentication.ui.newServerIntent
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.server.ui.ChangeServerActivity
import chat.rocket.android.server.ui.INTENT_CHAT_ROOM_ID

class ChangeServerNavigator (internal val activity: ChangeServerActivity) {

    fun toServerScreen() {
        activity.startActivity(activity.newServerIntent())
        activity.finish()
    }

    fun toChatRooms(chatRoomId: String? = null) {
        activity.startActivity(Intent(activity, MainActivity::class.java).also {
            it.putExtra(INTENT_CHAT_ROOM_ID, chatRoomId)
        })
        activity.finish()
    }

}