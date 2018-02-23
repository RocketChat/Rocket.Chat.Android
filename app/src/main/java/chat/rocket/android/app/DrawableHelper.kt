import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.widget.EditText
import android.widget.TextView
import chat.rocket.android.R

object DrawableHelper {

    /**
     * Returns a Drawable from its ID.
     *
     * @param id The drawable ID.
     * @param context The context.
     * @return A drawable.
     */
    fun getDrawableFromId(id: Int, context: Context): Drawable = context.resources.getDrawable(id, null)

    /**
     * Wraps an array of Drawable to be used for example for tinting.
     *
     * @param drawables The array of Drawable to wrap.
     * @see wrapDrawable
     * @see tintDrawables
     */
    fun wrapDrawables(drawables: Array<Drawable>) {
        for (drawable in drawables) {
            DrawableCompat.wrap(drawable)
        }
    }

    /**
     * Wraps the Drawable to be used for example for tinting.
     *
     * @param drawable The Drawable to wrap.
     * @see wrapDrawables
     * @see tintDrawable
     */
    fun wrapDrawable(drawable: Drawable): Drawable = DrawableCompat.wrap(drawable)

    /**
     * Tints an array of Drawable.
     *
     * REMARK: you MUST always wrap the array of Drawable before tint it.
     *
     * @param drawables The array of Drawable to tint.
     * @param context The context.
     * @param resId The resource id color to tint the Drawables.
     * @see tintDrawable
     * @see wrapDrawables
     */
    fun tintDrawables(drawables: Array<Drawable>, context: Context, resId: Int) {
        for (drawable in drawables) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, resId))
        }
    }

    /**
     * Tints a Drawable.
     *
     * REMARK: you MUST always wrap the Drawable before tint it.
     *
     * @param drawable The Drawable to tint.
     * @param context The context.
     * @param resId The resource id color to tint the Drawable.
     * @see tintDrawables
     * @see wrapDrawable
     */
    fun tintDrawable(drawable: Drawable, context: Context, resId: Int) = DrawableCompat.setTint(drawable, ContextCompat.getColor(context, resId))

    /**
     * Compounds an array of Drawable (to appear to the left of the text) into an array of TextView.
     *
     * REMARK: the number of elements in both array of Drawable and EditText MUST be equal.
     *
     * @param textView The array of TextView.
     * @param drawables The array of Drawable.
     * @see compoundDrawable
     */
    fun compoundDrawables(textView: Array<EditText>, drawables: Array<Drawable>) {
        if (textView.size != drawables.size) {
            return
        } else {
            for (i in textView.indices) {
                textView[i].setCompoundDrawablesWithIntrinsicBounds(drawables[i], null, null, null)
            }
        }
    }

    /**
     * Compounds a Drawable (to appear to the left of the text) into a TextView.
     *
     * @param textView The TextView.
     * @param drawable The Drawable.
     * @see compoundDrawables
     */
    fun compoundDrawable(textView: TextView, drawable: Drawable) = textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

    /**
     * Returns the user status drawable.
     *
     * @param userStatus The user status.
     * @param context The context.
     * @return The user status drawable.
     */
    fun getUserStatusDrawable(userStatus: String, context: Context): Drawable {
        val userStatusDrawable = getDrawableFromId(R.drawable.ic_user_status_black, context).mutate()
        wrapDrawable(userStatusDrawable)
        when (userStatus) {
            // TODO: create a enum or check if it will come from the SDK
            "online" -> tintDrawable(userStatusDrawable, context, R.color.colorUserStatusOnline)
            "busy" -> tintDrawable(userStatusDrawable, context, R.color.colorUserStatusBusy)
            "away" -> tintDrawable(userStatusDrawable, context, R.color.colorUserStatusAway)
            "offline" -> tintDrawable(userStatusDrawable, context, R.color.colorUserStatusOffline)
        }
        return userStatusDrawable
    }

    private val AVATAR_BACKGROUND_HEXADECIMAL_COLORS = intArrayOf(
            0xFFF44336.toInt(), 0xFFE91E63.toInt(), 0xFF9C27B0.toInt(), 0xFF673AB7.toInt(), 0xFF3F51B5.toInt(),
            0xFF2196F3.toInt(), 0xFF03A9F4.toInt(), 0xFF00BCD4.toInt(), 0xFF009688.toInt(), 0xFF4CAF50.toInt(),
            0xFF8BC34A.toInt(), 0xFFCDDC39.toInt(), 0xFFFFC107.toInt(), 0xFFFF9800.toInt(), 0xFFFF5722.toInt(),
            0xFF795548.toInt(), 0xFF9E9E9E.toInt(), 0xFF607D8B.toInt())

    /**
     * Returns a background color to be rendered on the avatar.
     *
     * @param string Gets the background color based on the provided string.
     * @return A hexadecimal color.
     * @see (Rocket.Chat/server/startup/avatar.js)
     */
    public fun getAvatarBackgroundColor(string: String): Int {
        return AVATAR_BACKGROUND_HEXADECIMAL_COLORS[string.length % AVATAR_BACKGROUND_HEXADECIMAL_COLORS.size]
    }
}