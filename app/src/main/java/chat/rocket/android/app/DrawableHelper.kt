import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.widget.EditText

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
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
     * @see tintDrawables
     */
    fun wrapDrawables(drawables: Array<Drawable>) {
        for (drawable in drawables) {
            DrawableCompat.wrap(drawable)
        }
    }

    /**
     * Tints an array of Drawable.
     *
     * REMARK: you MUST always wrap the array of Drawable before tint it.
     *
     * @param drawables The array of Drawable to tint.
     * @param context The context.
     * @param resId The resource id color to tint the Drawables.
     * @see wrapDrawables
     */
    fun tintDrawables(drawables: Array<Drawable>, context: Context, resId: Int) {
        for (drawable in drawables) {
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, resId))
        }
    }

    /**
     * Compounds an array of Drawable (to appear to the left of the text) into an array of EditText.
     *
     * REMARK: the number of elements in both array of Drawable and EditText MUST be equal.
     *
     * @param editTexts The array of EditText.
     * @param drawables The array of Drawable.
     */
    fun compoundDrawables(editTexts: Array<EditText>, drawables: Array<Drawable>) {
        if (editTexts.size != drawables.size) {
            return
        } else {
            for (i in editTexts.indices) {
                editTexts[i].setCompoundDrawablesWithIntrinsicBounds(drawables[i], null, null, null)
            }
        }
    }
}