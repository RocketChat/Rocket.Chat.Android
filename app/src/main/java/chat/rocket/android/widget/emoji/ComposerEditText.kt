package chat.rocket.android.widget.emoji

import android.content.Context
import android.graphics.Rect
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent

class ComposerEditText : AppCompatEditText {
    var listener: ComposerEditTextListener? = null
    var isKeyboardOpen = false

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
            super(context, attrs, defStyleAttr) {
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = true
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, android.support.v7.appcompat.R.attr.editTextStyle)

    constructor(context: Context) : this(context, null)

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            val state = getKeyDispatcherState()
            if (state != null) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    state.startTracking(event, this)
                    listener?.onKeyboardClosed()
                    isKeyboardOpen = false
                }
                return true
            }
        }
        return super.dispatchKeyEventPreIme(event)
    }

    override fun performClick(): Boolean {
        // Do not trigger event if the keyboard is already visible
        if (!isKeyboardOpen) {
            listener?.onKeyboardOpened()
            isKeyboardOpen = true
        }

        return super.performClick()
    }

    override fun onFocusChanged(focused: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        // To handle the first click that isn't being detected by performClick()
        if (focused && !isKeyboardOpen) {
            performClick()
        }

        super.onFocusChanged(focused, direction, previouslyFocusedRect)
    }

    interface ComposerEditTextListener {
        fun onKeyboardClosed()
        fun onKeyboardOpened()
    }
}