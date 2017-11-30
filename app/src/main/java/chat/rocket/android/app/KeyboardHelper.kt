package chat.rocket.android.app

import android.graphics.Rect
import android.view.View

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
}