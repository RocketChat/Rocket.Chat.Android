package chat.rocket.android.util

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
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
     * Wraps the Drawable to be used for example for tinting.
     *
     * @param drawable The Drawable to wrap.
     * @see wrapDrawables
     * @see tintDrawable
     */
    fun wrapDrawable(drawable: Drawable): Drawable = DrawableCompat.wrap(drawable)

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