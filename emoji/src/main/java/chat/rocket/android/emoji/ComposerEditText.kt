package chat.rocket.android.emoji

import android.content.Context
import android.text.Spanned
import android.text.style.ImageSpan
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.text.getSpans

class ComposerEditText : AppCompatEditText {

    var listener: ComposerEditTextListener? = null

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
        super(context, attrs, defStyleAttr) {
        isFocusable = true
        isFocusableInTouchMode = true
        isClickable = true
        isLongClickable = true
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, androidx.appcompat.R.attr.editTextStyle)

    constructor(context: Context) : this(context, null)

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        text?.getSpans<ImageSpan>()?.forEach {
            val s = text?.getSpanStart(it) ?: -1
            val e = text?.getSpanEnd(it) ?: -1
            val flags = if (selStart in s..e) {
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE or Spanned.SPAN_COMPOSING
            } else {
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            }

            text?.setSpan(it, s, e, flags)
        }
    }

    override fun dispatchKeyEventPreIme(event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            val state = keyDispatcherState
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
