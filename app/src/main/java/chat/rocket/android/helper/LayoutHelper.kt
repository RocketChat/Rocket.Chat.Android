package chat.rocket.android.helper

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import chat.rocket.android.util.TimberLogger

class LayoutHelper {
    private var childOfContent: View? = null
    private var usableHeightPrevious: Int = 0
    private var frameLayoutParams: FrameLayout.LayoutParams? = null
    private val listener = ViewTreeObserver.OnGlobalLayoutListener { resizeChildOfContent() }

    /**
     * Workaround to adjust the layout when in the full screen mode.
     *
     * The original author of this code is Joseph Johnson and you can see his answer here: https://stackoverflow.com/a/19494006/4744263
     *
     * Note that this function has some differences from the original, like using *frameLayoutParams.height = usableHeightNow* instead of
     * *frameLayoutParams.height = usableHeightSansKeyboard* (RobertoAllende's comment - from the same link above).
     *
     * @param activity The Activity to adjust the layout.
     */
    fun install(activity: Activity) {
        try {
            val content = activity.findViewById<View>(android.R.id.content) as FrameLayout
            childOfContent = content.getChildAt(0)
            childOfContent?.viewTreeObserver?.addOnGlobalLayoutListener(listener)
            frameLayoutParams = childOfContent?.layoutParams as FrameLayout.LayoutParams
        } catch (exception: ClassCastException) {
            TimberLogger.warn(exception.message.toString())
        }
    }

    fun remove() {
        childOfContent?.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
        childOfContent = null
        frameLayoutParams = null
    }

    private fun resizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = childOfContent?.rootView?.height ?: 0
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                frameLayoutParams?.height = usableHeightSansKeyboard - heightDifference
            } else {
                // keyboard probably just became hidden
                frameLayoutParams?.height = usableHeightNow
            }
            childOfContent?.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight(): Int {
        val rect = Rect()
        childOfContent?.getWindowVisibleDisplayFrame(rect)
        return rect.bottom - rect.top
    }
}