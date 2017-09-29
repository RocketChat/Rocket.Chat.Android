package chat.rocket.android.widget.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat

/**
 * Created by Filipe de Lima Brito (filipedelimabrito@gmail.com) on 8/29/17.
 */
object DrawableHelper {

    /**
     * Returns a bitmap from drawable.
     *
     * @param drawable The drawable to get the bitmap.
     * @return A bitmap.
     */
    fun getBitmapFromDrawable(drawable: Drawable, intrinsicWidth: Int = 1, intrinsicHeight: Int = 1): Bitmap {
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val textDrawableIntrinsicWidth = drawable.intrinsicWidth
        var textDrawableIntrinsicHeight = drawable.intrinsicHeight

        val width = if (textDrawableIntrinsicWidth > 0) textDrawableIntrinsicWidth else intrinsicWidth
        val height = if (textDrawableIntrinsicHeight > 0) textDrawableIntrinsicHeight else intrinsicHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return bitmap
    }

    /**
     * Wraps a drawable to be used for example for tinting.
     *
     * @param drawable The drawable to wrap.
     * @see tintDrawable
     */
    fun wrapDrawable(drawable: Drawable?) {
        if (drawable != null) {
            DrawableCompat.wrap(drawable)
        }
    }

    /**
     * REMARK: You MUST always wrap the drawable before tint it.
     *
     * @param drawable The drawable to tint.
     * @param context The context.
     * @param resId The resource id color to tint the drawable.
     * @see wrapDrawable
     */
    fun tintDrawable(drawable: Drawable?, context: Context, resId: Int) {
        if (drawable != null) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, resId))
        }
    }
}