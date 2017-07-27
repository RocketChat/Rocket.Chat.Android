package chat.rocket.android.renderer

import chat.rocket.android.R
import chat.rocket.android.widget.helper.UserStatusProvider
import chat.rocket.core.models.User

object RocketChatUserStatusProvider: UserStatusProvider {

    override fun getStatusResId(status: String?): Int {
        var userStatusDrawableId = R.drawable.userstatus_offline
        when (status) {
            User.STATUS_ONLINE -> {
                userStatusDrawableId = R.drawable.userstatus_online
            }
            User.STATUS_AWAY -> {
                userStatusDrawableId = R.drawable.userstatus_away
            }
            User.STATUS_BUSY -> {
                userStatusDrawableId = R.drawable.userstatus_busy
            }
        }
        return userStatusDrawableId
    }
}