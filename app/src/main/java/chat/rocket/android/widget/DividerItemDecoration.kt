package chat.rocket.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView

/**
 * Adds a default or custom divider to specific item views from the adapter's data set.
 * @see RecyclerView.ItemDecoration
 */
class DividerItemDecoration() : RecyclerView.ItemDecoration() {
    private lateinit var divider: Drawable
    private var boundStart = 0
    private var boundRight = 0

    // Default divider will be used.
    constructor(context: Context) : this() {
        val attrs = intArrayOf(android.R.attr.listDivider)
        val styledAttributes = context.obtainStyledAttributes(attrs)
        divider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()
    }

    // Default divider with custom boundaries (start and right) will be used.
    constructor(context: Context, boundStart: Int, boundRight: Int) : this() {
        val attrs = intArrayOf(android.R.attr.listDivider)
        val styledAttributes = context.obtainStyledAttributes(attrs)
        divider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()

        this.boundStart = boundStart
        this.boundRight = boundRight
    }

    // Custom divider will be used.
    constructor(context: Context, @DrawableRes drawableResId: Int) : this() {
        val customDrawable = ContextCompat.getDrawable(context, drawableResId)
        if (customDrawable != null) {
            divider = customDrawable
        }
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft + boundStart
        val right = (parent.width - parent.paddingRight) - boundRight

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }
}