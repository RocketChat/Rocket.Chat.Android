package chat.rocket.android.util.extensions

import android.app.Activity
import android.net.Uri
import android.widget.TextView
import android.provider.OpenableColumns



fun String.ifEmpty(value: String): String {
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

fun Uri.getFileName(activity: Activity): String? {
    val cursor = activity.contentResolver.query(this, null, null, null, null, null)

    var fileName: String? = null
    cursor.use { cursor ->
        if (cursor != null && cursor.moveToFirst()) {
            fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }
    return fileName
}

fun Uri.getFileSize(activity: Activity): String? {
    val cursor = activity.contentResolver.query(this, null, null, null, null, null)

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