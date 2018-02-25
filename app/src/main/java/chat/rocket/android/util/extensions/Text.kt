package chat.rocket.android.util.extensions

import android.widget.TextView
import ru.noties.markwon.Markwon

fun String.ifEmpty(value: String): String {
    if (isEmpty()) {
        return value
    }
    return this
}

fun CharSequence.ifEmpty(value: String): CharSequence {
    if (isEmpty()) {
        return value
    }
    return this
}

var TextView.textContent: String
    get() = text.toString()
    set(value) {
        text = value
    }

var TextView.hintContent: String
    get() = hint.toString()
    set(value) {
        hint = value
    }

var TextView.content: CharSequence
    get() = text
    set(value) {
        Markwon.unscheduleDrawables(this)
        Markwon.unscheduleTableRows(this)
        text = value
        Markwon.scheduleDrawables(this)
        Markwon.scheduleTableRows(this)
    }