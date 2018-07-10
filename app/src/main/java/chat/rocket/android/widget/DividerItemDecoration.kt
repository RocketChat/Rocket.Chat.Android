package chat.rocket.android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/**
 * Adds a default or custom divider to specific item views from the adapter's data set.
 * @see RecyclerView.ItemDecoration
 */
class DividerItemDecoration() : RecyclerView.ItemDecoration() {
    private lateinit var divider: Drawable
    private var boundStart = 0
    private var boundEnd = 0

    // Default divider will be used.
    constructor(context: Context) : this() {
        val attrs = intArrayOf(android.R.attr.listDivider)
        val styledAttributes = context.obtainStyledAttributes(attrs)
        divider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()
    }

    // Default divider with custom boundaries (start and end) will be used.
    constructor(context: Context, boundStart: Int, boundEnd: Int) : this() {
        val attrs = intArrayOf(android.R.attr.listDivider)
        val styledAttributes = context.obtainStyledAttributes(attrs)
        divider = styledAttributes.getDrawable(0)
        styledAttributes.recycle()

        this.boundStart = boundStart
        this.boundEnd = boundEnd
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
        val right = (parent.width - parent.paddingRight) - boundEnd

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)

            if (isLastView(child, parent))
                continue

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight

            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

    private fun isLastView(view: View, parent: RecyclerView): Boolean {
        val position = parent.getChildAdapterPosition(view)
        return position == parent.adapter?.itemCount?.minus(1) ?: false
    }
}