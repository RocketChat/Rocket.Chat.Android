package chat.rocket.android.suggestions.ui

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.suggestions.R

internal class PopupRecyclerView : RecyclerView {
    private var displayWidth: Int = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = DisplayMetrics()
        display.getMetrics(size)
        val screenWidth = size.widthPixels
        displayWidth = screenWidth
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val hSpec = MeasureSpec.makeMeasureSpec(
            resources.getDimensionPixelSize(
                R.dimen.popup_max_height
            ), MeasureSpec.AT_MOST
        )
        val wSpec = MeasureSpec.makeMeasureSpec(displayWidth, MeasureSpec.EXACTLY)
        super.onMeasure(wSpec, hSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l + 40, t, r - 40, b)
    }
}
