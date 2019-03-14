/**
 * Copyright 2015 YA LLC
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package chat.rocket.android.emoji

import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.PopupWindow

/**
 * Base class to create popup window that appears over software keyboard.
 */
abstract class OverKeyboardPopupWindow(
    val context: Context,
    private val rootView: View
) : PopupWindow(context), ViewTreeObserver.OnGlobalLayoutListener {

    /**
     * @return keyboard height in pixels
     */
    var keyboardHeight = 0
        private set
    private var pendingOpen = false
    /**
     * @return Returns true if the soft keyboard is open, false otherwise.
     */
    var isKeyboardOpen = false
        private set

    private var keyboardHideListener: OnKeyboardHideListener? = null

    interface OnKeyboardHideListener {
        fun onKeyboardHide()
    }

    init {
        setBackgroundDrawable(null)
        val view = onCreateView(LayoutInflater.from(context))
        onViewCreated(view)
        contentView = view
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE

        // Default size
        setSize(this.context.resources.getDimensionPixelSize(R.dimen.supposed_keyboard_height),
                WindowManager.LayoutParams.MATCH_PARENT)
        setSizeForSoftKeyboard()
    }

    fun setKeyboardHideListener(keyboardHideListener: OnKeyboardHideListener) {
        this.keyboardHideListener = keyboardHideListener
    }

    /**
     * Manually set the popup window size
     *
     * @param width  Width of the popup
     * @param height Height of the popup
     */
    fun setSize(width: Int, height: Int) {
        setWidth(width)
        setHeight(height)
    }

    /**
     * Call this function to resize the emoji popup according to your soft keyboard size
     */
    private fun setSizeForSoftKeyboard() {
        val viewTreeObserver = rootView.viewTreeObserver
        viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        val r = Rect()
        rootView.getWindowVisibleDisplayFrame(r)

        val screenHeight = calculateScreenHeight()
        var heightDifference = screenHeight - (r.bottom - r.top)

        val resources = context.resources
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            heightDifference -= resources.getDimensionPixelSize(resourceId)
        }

        if (heightDifference > 100) {
            keyboardHeight = heightDifference
            setSize(WindowManager.LayoutParams.MATCH_PARENT, keyboardHeight)

            isKeyboardOpen = true
            if (pendingOpen) {
                showAtBottom()
                pendingOpen = false
            }
        } else {
            if (isKeyboardOpen && keyboardHideListener != null) {
                keyboardHideListener!!.onKeyboardHide()
            }
            isKeyboardOpen = false
        }
    }

    private fun calculateScreenHeight(): Int {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = wm.defaultDisplay
        val size = Point()
        display.getSize(size)
        return size.y
    }

    /**
     * Use this function to show the popup.
     * NOTE: Since, the soft keyboard sizes are variable on different android devices, the
     * library needs you to open the soft keyboard at least once before calling this function.
     * If that is not possible see showAtBottomPending() function.
     */
    fun showAtBottom() {
        showAtLocation(rootView, Gravity.BOTTOM, 0, 0)
    }

    /**
     * Use this function when the soft keyboard has not been opened yet. This
     * will show the popup after the keyboard is up next time.
     * Generally, you will be calling InputMethodManager.showSoftInput function after
     * calling this function.
     */
    fun showAtBottomPending() {
        if (isKeyboardOpen) {
            showAtBottom()
        } else {
            pendingOpen = true
        }
    }

    abstract fun onCreateView(inflater: LayoutInflater): View

    abstract fun onViewCreated(view: View)
}