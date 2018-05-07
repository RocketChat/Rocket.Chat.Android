package chat.rocket.android.server.presentation

import android.content.Intent
import chat.rocket.android.authentication.ui.newServerIntent
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.server.ui.ChangeServerActivity

class ChangeServerNavigator (internal val activity: ChangeServerActivity) {
    fun toServerScreen() {
        activity.startActivity(activity.newServerIntent())
        activity.finish()
    }

    fun toChatRooms() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

}