package chat.rocket.android.widget.helper

import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import chat.rocket.android.widget.AbsoluteUrl
import com.amulyakhare.textdrawable.TextDrawable
import java.net.URLEncoder

object AvatarHelper {

    /**
     * Returns the user avatar URI.
     *
     * REMARK: This is often a SVG image (Rocket.Chat:server/startup/avatar.js).
     *
     * @param hostname The server's hostname.
     * @param username The username.
     * @return The user avatar URI.
     */
    fun getUri(hostname : String, username: String): String {
        return "https://" +
                hostname.replace("http://", "").replace("https://", "") +
                "/avatar/" +
                URLEncoder.encode(username, "UTF-8")
    }

    /**
     * Returns the user avatar absolute URI.
     *
     * REMARK: This is often a SVG image (Rocket.Chat:server/startup/avatar.js).
     *
     * @param absoluteUrl The AbsoluteUrl.
     * @param username The username.
     * @return The user avatar absolute URI.
     */
    fun getAbsoluteUri(absoluteUrl: AbsoluteUrl, username: String): String {
        val avatarUri = "/avatar/" + URLEncoder.encode(username, "UTF-8")
        return absoluteUrl.from(avatarUri)
    }

    /**
     * Returns a drawable with username initials.
     *
     * @param username The username.
     * @param context The context.
     * @return A drawable with username initials.
     * @see getUsernameInitials
     */
    fun getTextDrawable(username: String, context: Context): Drawable {
        val round = (4 * context.resources.displayMetrics.density).toInt()

        return TextDrawable.builder()
                .beginConfig()
                .useFont(Typeface.SANS_SERIF)
                .endConfig()
                .buildRoundRect(getUsernameInitials(username), getUserAvatarBackgroundColor(username), round)
    }

    /**
     * Returns a string with the username initials. For example: username John.Doe returns JD initials.
     *
     * @param username The username.
     * @return A string with username initials.
     */
    fun getUsernameInitials(username: String): String {
        if (username.isEmpty()) {
            return "?"
        }

        val splitUsername = username.split(".")
        val splitCount = splitUsername.size
        if (splitCount > 1 && splitUsername[0].isNotEmpty() && splitUsername[splitCount-1].isNotEmpty()) {
            val firstInitial = splitUsername[0].substring(0, 1)
            val secondInitial = splitUsername[splitCount-1].substring(0, 1)
            return (firstInitial + secondInitial).toUpperCase()
        } else {
            if (username.length > 1) {
                return username.substring(0, 2).toUpperCase()
            } else {
                return username.substring(0, 1).toUpperCase()
            }
        }
    }

    /**
     * Returns a background color to be rendered on the user avatar (Rocket.Chat:server/startup/avatar.js).
     *
     * @param username The username.
     * @return A hexadecimal color.
     */
    fun getUserAvatarBackgroundColor(username: String): Int {
        return COLORS[username.length % COLORS.size]
    }

    private val COLORS = intArrayOf(
            0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 0xFF673AB7.toInt(), 0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(), 0xFF03A9F4.toInt(), 0xFF00BCD4.toInt(), 0xFF009688.toInt(), 0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(), 0xFFCDDC39.toInt(), 0xFFFFC107.toInt(), 0xFFFF9800.toInt(), 0xFFFF5722.toInt(),
            0xFF795548.toInt(), 0xFF9E9E9E.toInt(), 0xFF607D8B.toInt())
}