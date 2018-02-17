package chat.rocket.android.util.extensions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.widget.TextView
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.provider.MediaStore
import android.text.Spannable
import android.text.Spanned
import android.text.TextUtils
import chat.rocket.android.widget.emoji.EmojiParser
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
        if (value is Spanned) {
            val result = EmojiParser.parse(value.toString()) as Spannable
            TextUtils.copySpansFrom(value, 0, value.length, Any::class.java, result, 0)
            text = result
        } else {
            val result = EmojiParser.parse(value.toString()) as Spannable
            text = result
        }
        Markwon.scheduleDrawables(this)
        Markwon.scheduleTableRows(this)
    }

fun Uri.getFileName(context: Context): String? {
    val cursor = context.contentResolver.query(this, null, null, null, null, null)

    var fileName: String? = null
    cursor.use { cursor ->
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }
    return fileName
}

fun Uri.getFileSize(context: Context): String? {
    val cursor = context.contentResolver.query(this, null, null, null, null, null)

    var fileSize: String? = null
    cursor.use { cursor ->
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor != null && cursor.moveToFirst()) {
            if (!cursor.isNull(sizeIndex)) {
                fileSize = cursor.getString(sizeIndex)
            }
        }
    }
    return fileSize
}

fun Uri.getMimeType(context: Context): String {
    return if (scheme == ContentResolver.SCHEME_CONTENT) {
        context.contentResolver.getType(this)
    } else {
        val fileExtension = MimeTypeMap.getFileExtensionFromUrl(toString())
        MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase())
    }
}

fun Uri.getRealPathFromURI(context: Context): String? {
    val cursor = context.contentResolver.query(this, arrayOf(MediaStore.Images.Media.DATA), null, null, null)

    cursor.use { cursor ->
        val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }
}