package chat.rocket.android.util

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

class RCRecyclerView : RecyclerView {

    private var dispatchTouchStatus: Boolean = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs,
            defStyle)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (!dispatchTouchStatus) false
        else super.dispatchTouchEvent(ev)
    }

    fun setDispatchTouchStatus(status: Boolean) = apply { dispatchTouchStatus = status }
}
