package chat.rocket.android.helper

import java.net.URLEncoder

class RocketChatUserAvatar(val hostname: String, val username: String) {

    val imageUri: String
        /** REMARK
         * This is often a SVG image (see Rocket.Chat:server/startup/avatar.js).
         */
        get() {
            return "https://" +
                    hostname.replace("http://", "").replace("https://", "") +
                    "/avatar/" +
                    URLEncoder.encode(username, "UTF-8")
        }
}