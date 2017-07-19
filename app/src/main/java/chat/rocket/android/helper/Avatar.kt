package chat.rocket.android.helper

import chat.rocket.android.widget.AbsoluteUrl
import chat.rocket.android.widget.RocketChatAvatar
import java.net.URLEncoder

class Avatar(val absoluteUrl: AbsoluteUrl, val username: String) {

    val imageUrl: String
        /** REMARK
         * This is often a SVG image (see Rocket.Chat:server/startup/avatar.js)
         */
        get() {
            return absoluteUrl.from("/avatar/" + URLEncoder.encode(username, "UTF-8"))
        }

    /**
     * render avatar into RocketChatAvatar.
     */
    fun into(rocketChatAvatar: RocketChatAvatar) {
        rocketChatAvatar.loadImage(imageUrl)
    }
}