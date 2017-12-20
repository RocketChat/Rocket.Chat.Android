package chat.rocket.android.util

import android.view.View
import android.widget.TextView

fun String.ifEmpty(value: String): String {
    if (isEmpty()) {
        return value
    }
    return this
}

fun View.setVisibility(value: Boolean) {
    if (value) {
        this.visibility = View.VISIBLE
    } else {
        this.visibility = View.GONE
    }
}

var TextView.textContent: String
    get() = this.text.toString()
    set(value) {
        this.text = value
    }

var TextView.hintContent: String
    get() = this.hint.toString()
    set(value) {
        this.text = value
    }
