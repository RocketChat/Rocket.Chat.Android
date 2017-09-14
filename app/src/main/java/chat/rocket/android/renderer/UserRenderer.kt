package chat.rocket.android.renderer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.widget.RocketChatAvatar
import chat.rocket.android.widget.helper.AvatarHelper
import chat.rocket.core.models.User

class UserRenderer(val user: User) {

    /**
     * Show user's avatar image in RocketChatAvatar widget.
     */
    fun showAvatar(rocketChatAvatarWidget: RocketChatAvatar, hostname: String) {
        val username: String? = user.username
        if (username != null) {
            rocketChatAvatarWidget.loadImage(AvatarHelper.getUri(hostname, username), AvatarHelper.getTextDrawable(username, rocketChatAvatarWidget.context))
        } else {
            rocketChatAvatarWidget.visibility = View.GONE
        }
    }

    /**
     * Show username in textView.
     */
    fun showUsername(textView: TextView) {
        val username: String? = user.username
        if (username != null) {
            textView.text = username
        }
    }

    /**
     * Show user's status color in imageView.
     */
    fun showStatusColor(imageView: ImageView) {
        val userStatus: String? = user.status
        if (userStatus != null) {
            imageView.setImageResource(RocketChatUserStatusProvider.getStatusResId(userStatus))
        } else {
            imageView.visibility = View.GONE
        }
    }
}