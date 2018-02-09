package chat.rocket.android.util.extensions

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.widget.TextView
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import android.provider.MediaStore

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