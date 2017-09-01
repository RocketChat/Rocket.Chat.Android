package chat.rocket.android.widget.helper

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat

object DrawableHelper {

    /**
     * Wraps a drawable to be used for example for tinting.
     *
     * @param drawable The drawable to wrap.
     * @see tintDrawable
     */
    fun wrapDrawable(drawable: Drawable) {
        DrawableCompat.wrap(drawable)
    }

    /**
     * REMARK: You MUST always wrap the drawable before tint it.
     *
     * @param drawable The drawable to tint.
     * @param context The context.
     * @param resId The resource id color to tint the drawable.
     * @see wrapDrawable
     */
    fun tintDrawable(drawable: Drawable, context: Context, resId: Int) {
        DrawableCompat.setTint(drawable, ContextCompat.getColor(context, resId))
    }
}