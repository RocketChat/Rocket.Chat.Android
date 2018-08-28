package chat.rocket.android.util.extensions

import android.text.Spannable
import android.text.Spanned
import android.text.TextUtils
import android.util.Base64
import android.util.Patterns
import android.widget.TextView
import chat.rocket.android.emoji.EmojiParser
import org.json.JSONObject
import ru.noties.markwon.Markwon
import java.net.URLDecoder
import java.security.SecureRandom

fun String.ifEmpty(value: String): String {
    if (isEmpty()) {
        return value
    }
    return this
}

fun String.isEmail(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.encodeToBase64(): String {
    return Base64.encodeToString(this.toByteArray(charset("UTF-8")), Base64.NO_WRAP)
}

fun String.decodeFromBase64(): String {
    return Base64.decode(this, Base64.DEFAULT).toString(charset("UTF-8"))
}

fun String.decodeUrl(): String {
    return URLDecoder.decode(this, "UTF-8")
}

fun String.toJsonObject(): JSONObject {
    return JSONObject(this)
}

fun generateRandomString(stringLength: Int): String {
    val base = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
    val secureRandom = SecureRandom()

    val stringBuilder = StringBuilder(stringLength)
    for (i in 0 until stringLength) {
        stringBuilder.append(base[secureRandom.nextInt(base.length)])
    }
    return stringBuilder.toString()
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
            val context = this.context
            val result = EmojiParser.parse(context, value.toString()) as Spannable
            val end = if (value.length > result.length) result.length else value.length
            TextUtils.copySpansFrom(value, 0, end, Any::class.java, result, 0)
            text = result
        } else {
            val result = EmojiParser.parse(context, value.toString()) as Spannable
            text = result
        }
        Markwon.scheduleDrawables(this)
        Markwon.scheduleTableRows(this)
    }
