package chat.rocket.android.util

import android.support.annotation.LayoutRes
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

fun ViewGroup.inflate(@LayoutRes resource: Int): View {
    return LayoutInflater.from(context).inflate(resource, this, false)
}

var TextView.textContent: String
    get() = text.toString()
    set(value) {
        text = value
    }

var TextView.hintContent: String
    get() = hint.toString()
    set(value) {
        text = value
    }
