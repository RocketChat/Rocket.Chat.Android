package chat.rocket.android.app

import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.widget.FrameLayout

//TODO: check if this code has memory leak.
object LayoutHelper {
    private lateinit var childOfContent: View
    private var usableHeightPrevious: Int = 0
    private lateinit var frameLayoutParams: FrameLayout.LayoutParams

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
    fun androidBug5497Workaround(activity: Activity) {
        try {
            val content = activity.findViewById<View>(android.R.id.content) as FrameLayout
            childOfContent = content.getChildAt(0)
            childOfContent.viewTreeObserver.addOnGlobalLayoutListener({ resizeChildOfContent() })
            frameLayoutParams = childOfContent.layoutParams as FrameLayout.LayoutParams
        } catch (exception : ClassCastException) {
            // TODO: are we using the android.util.Log for logging that type of errors? or should we use the SDK logger?
            Log.e("ERROR", exception.message)
        }
    }

    private fun resizeChildOfContent() {
        val usableHeightNow = computeUsableHeight()
        if (usableHeightNow != usableHeightPrevious) {
            val usableHeightSansKeyboard = childOfContent.rootView.height
            val heightDifference = usableHeightSansKeyboard - usableHeightNow
            if (heightDifference > usableHeightSansKeyboard / 4) {
                // keyboard probably just became visible
                frameLayoutParams.height = usableHeightSansKeyboard - heightDifference
            } else {
                // keyboard probably just became hidden
                frameLayoutParams.height = usableHeightNow
            }
            childOfContent.requestLayout()
            usableHeightPrevious = usableHeightNow
        }
    }

    private fun computeUsableHeight(): Int {
        val rect = Rect()
        childOfContent.getWindowVisibleDisplayFrame(rect)
        return rect.bottom - rect.top
    }
}