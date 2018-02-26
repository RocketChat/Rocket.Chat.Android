package chat.rocket.android.util.extensions

import android.text.Spannable
import android.text.Spanned
import android.text.TextUtils
import android.widget.EditText
import android.widget.TextView
import chat.rocket.android.widget.emoji.EmojiParser
import chat.rocket.android.widget.emoji.EmojiTypefaceSpan
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

fun EditText.erase() {
    this.text.clear()
    val spans = this.text.getSpans(0, text.length, EmojiTypefaceSpan::class.java)
    spans.forEach {
        text.removeSpan(it)
    }
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

var TextView.content: CharSequence?
    get() = text
    set(value) {
        Markwon.unscheduleDrawables(this)
        Markwon.unscheduleTableRows(this)
        if (value is Spanned) {
            val result = EmojiParser.parse(value.toString()) as Spannable
            val end = if (value.length > result.length) result.length else value.length
            TextUtils.copySpansFrom(value, 0, end, Any::class.java, result, 0)
            text = result
        } else {
            val result = EmojiParser.parse(value.toString()) as Spannable
            text = result
        }
        Markwon.scheduleDrawables(this)
        Markwon.scheduleTableRows(this)
    }