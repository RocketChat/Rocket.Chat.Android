package chat.rocket.android.util.extensions

import android.annotation.TargetApi
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream

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

fun Uri.getFileSize(context: Context): Int {
    val cursor = context.contentResolver.query(this, null, null, null, null, null)

    val fileSize = cursor?.use {
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
            if (!cursor.isNull(sizeIndex)) {
                return@use cursor.getString(sizeIndex)
            }
        }
        return@use null
    }
    return fileSize?.toIntOrNull() ?: -1
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

@TargetApi(Build.VERSION_CODES.N)
fun Uri.isVirtualFile(context: Context): Boolean {
    if (!DocumentsContract.isDocumentUri(context, this)) {
        return false
    }

    val cursor = context.contentResolver.query(this,
            arrayOf(DocumentsContract.Document.COLUMN_FLAGS),
            null, null, null)

    var flags = 0
    if (cursor.moveToFirst()) {
        flags = cursor.getInt(0)
    }
    cursor.close()

    return flags and DocumentsContract.Document.FLAG_VIRTUAL_DOCUMENT != 0
}

@Throws(IOException::class)
fun Uri.getInputStreamForVirtualFile(context: Context, mimeTypeFilter: String): FileInputStream? {

    val resolver = context.contentResolver

    val openableMimeTypes = resolver.getStreamTypes(this, mimeTypeFilter)

    if (openableMimeTypes == null || openableMimeTypes.isEmpty()) {
        throw FileNotFoundException()
    }

    return resolver.openTypedAssetFileDescriptor(this, openableMimeTypes[0],
            null)?.createInputStream()
}

fun Uri.getInputStream(context: Context): InputStream? {
    if (isVirtualFile(context)) {
        return getInputStreamForVirtualFile(context, "*/*")
    }

    return context.contentResolver.openInputStream(this)
}