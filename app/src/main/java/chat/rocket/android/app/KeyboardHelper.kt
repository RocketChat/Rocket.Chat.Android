package chat.rocket.android.app

import android.content.res.Configuration
import android.graphics.Rect
import android.view.View

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
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
     * Returns true if the hard keyboard is shown, false otherwise.
     *
     * @param newConfig The configuration.
     * @return true if the hard keyboard is shown, false otherwise.
     */
    fun isHardKeyboardShown(newConfig: Configuration?): Boolean {
        if (newConfig?.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO) {
            return true
        }
        return false
    }
}