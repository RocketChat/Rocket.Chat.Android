import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.common.model.UserStatus

object DrawableHelper {

    /**
     * Returns a Drawable from its ID.
     *
     * @param id The drawable ID.
     * @param context The context.
     * @return A drawable.
     */
    fun getDrawableFromId(id: Int, context: Context): Drawable =
        context.resources.getDrawable(id, null)

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
    fun tintDrawable(drawable: Drawable, context: Context, resId: Int) =
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, resId))

    /**
     * Compounds an array of Drawable (to appear to the left of the text) into an array of TextView.
     *
     * REMARK: the number of elements in both array of Drawable and EditText MUST be equal.
     *
     * @param textView The array of TextView.
     * @param drawables The array of Drawable.
     * @see compoundDrawable
     */
    fun compoundDrawables(textView: Array<TextView>, drawables: Array<Drawable>) {
        if (textView.size != drawables.size) {
            return
        } else {
            for (i in textView.indices) {
                textView[i].setCompoundDrawablesWithIntrinsicBounds(drawables[i], null, null, null)
            }
        }
    }

    /**
     * Compounds a Drawable (to appear on the left side of a text) into a TextView.
     *
     * @param textView The TextView.
     * @param drawable The Drawable.
     * @see compoundDrawables
     */
    fun compoundDrawable(textView: TextView, drawable: Drawable) =
        textView.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null)

    /**
     * Compounds a Drawable (to appear on the right side of a text) into a TextView.
     *
     * @param textView The TextView.
     * @param drawable The Drawable.
     * @see compoundDrawable
     */
    fun compoundRightDrawable(textView: TextView, drawable: Drawable) =
        textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)

    /**
     * Returns the user status drawable.
     *
     * @param userStatus The user status.
     * @param context The context.
     * @see [UserStatus]
     * @return The user status drawable.
     */
    fun getUserStatusDrawable(userStatus: UserStatus?, context: Context): Drawable {
        return when (userStatus) {
            is UserStatus.Online -> getDrawableFromId(R.drawable.ic_status_online_12dp, context)
            is UserStatus.Away -> getDrawableFromId(R.drawable.ic_status_away_12dp, context)
            is UserStatus.Busy -> getDrawableFromId(R.drawable.ic_status_busy_12dp, context)
            else -> getDrawableFromId(R.drawable.ic_status_invisible_12dp, context)
        }
    }
}