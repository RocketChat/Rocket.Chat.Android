package chat.rocket.android.util

import android.widget.TextView

fun String.ifEmpty(value: String): String {
    if (isEmpty()) {
        return value
    }
    return this
}

var TextView.content: String
    get() = this.text.toString()
    set(value) { this.text = value }