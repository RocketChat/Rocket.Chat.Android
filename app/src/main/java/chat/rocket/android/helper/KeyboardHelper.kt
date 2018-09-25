package chat.rocket.android.helper

import android.app.Activity
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager


object KeyboardHelper {

    /**
     * Returns true if the soft keyboard is shown, false otherwise.
     *
     * @param rootView The rootView of a view (e.g. an EditText).
     * @return true if the soft keyboard is shown, false otherwise.
     */
    fun isSoftKeyboardShown(rootView: View): Boolean {
        val softKeyboardHeight = 100
        val rect = Rect()

        rootView.getWindowVisibleDisplayFrame(rect)

        val dm = rootView.resources.displayMetrics
        val heightDiff = rootView.bottom - rect.bottom
        return heightDiff > softKeyboardHeight * dm.density
    }

    /**
     * Hide the soft keyboard.
     *
     * @param activity The current focused activity.
     */
    fun hideSoftKeyboard(activity: Activity) {
        val currentFocus = activity.currentFocus
        if (currentFocus != null) {
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }

    /**
     * Show the soft keyboard for the given view.
     *
     * @param view View to receive input focus.
     */
    fun showSoftKeyboard(view: View) {
        if (view.requestFocus()) {
            val inputMethodManager = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, InputMethodManager.SHOW_IMPLICIT)
        }
    }
}
