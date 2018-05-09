package chat.rocket.android.util

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

//hack to get the path of an image for android 6. 
//Credits to https://stackoverflow.com/questions/33208911/get-realpath-return-null-on-android-marshmallow
public fun getPath(context: Context, uri: Uri): String? {
    //The following boolean variable is added so that the hack works when sdk version is reduced in the future
    val isKitKat: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    // DocumentProvider
    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
        // ExternalStorageProvider
        if (isExternalStorageDocument(uri)) {
            val docId: String = DocumentsContract.getDocumentId(uri);
            val split = docId.split(":");
            val type: String = split[0]

            if ("primary".equals(type, true)) {
                return ("" + Environment.getExternalStorageDirectory() + "/" + split[1])
            }
        }

        // DownloadsProvider
        else if (isDownloadsDocument(uri)) {
            val id = DocumentsContract.getDocumentId(uri);
            val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), id.toLong())
            return getDataColumn(context, contentUri, null, null);
        }

        // MediaProvider
        else if (isMediaDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri);
            val split = docId.split(":");
            val type = split[0];

            var contentUri: Uri? = null;
            if ("image".equals(type)) {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            } else if ("video".equals(type)) {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
            } else if ("audio".equals(type)) {
                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            }
            val selection = "_id=?";
            val selectionArgs = arrayOf(split[1])

            return getDataColumn(context, contentUri, selection, selectionArgs);
        }
    }

    // MediaStore (and general)
    else if ("content".equals(uri.getScheme(), true)) {

        // Return the remote address
        if (isGooglePhotosUri(uri))
            return uri.getLastPathSegment();

        return getDataColumn(context, uri, null, null);
    }

    // File
    else if ("file".equals(uri.getScheme(), true)) {
        return uri.getPath()
    }

    return null
}

/**
 * Get the value of the data column for this Uri. This is useful for
 * MediaStore Uris, and other file-based ContentProviders.
 *
 * @param context The context.
 * @param uri The Uri to query.
 * @param selection (Optional) Filter used in the query.
 * @param selectionArgs (Optional) Selection arguments used in the query.
 * @return The value of the _data column, which is typically a file path.
 */
public fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                         selectionArgs: Array<String>?): String? {

    var cursor: Cursor? = null;
    val column = "_data";
    val projection = arrayOf(column)

    try {
        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(column);
            return cursor.getString(index);
        }
    } finally {
        if (cursor != null)
            cursor.close();
    }
    return null;
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is ExternalStorageProvider.
 */
public fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents".equals(uri.getAuthority());
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is DownloadsProvider.
 */
public fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is MediaProvider.
 */
public fun isMediaDocument(uri: Uri): Boolean {
    return "com.android.providers.media.documents".equals(uri.getAuthority());
}

/**
 * @param uri The Uri to check.
 * @return Whether the Uri authority is Google Photos.
 */
public fun isGooglePhotosUri(uri: Uri): Boolean {
    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
}
