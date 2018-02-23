package chat.rocket.android.widget.emoji

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent

class ComposerEditText : AppCompatEditText {
    var listener: ComposerEditTextListener? = null

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
                }
                return true
            }
        }
        return super.dispatchKeyEventPreIme(event)
    }

    override fun performClick(): Boolean {
        listener?.onKeyboardOpened()
        return super.performClick()
    }

    interface ComposerEditTextListener {
        fun onKeyboardClosed()
        fun onKeyboardOpened()
    }
}