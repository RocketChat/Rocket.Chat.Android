package chat.rocket.android.chatroom.domain

import android.content.Context
import android.net.Uri
import chat.rocket.android.util.extensions.*
import javax.inject.Inject



class UriInteractor @Inject constructor(private val context: Context) {

    /**
     * Gets the file name from an [Uri].
     */
    fun getFileName(uri: Uri): String? = uri.getFileName(context)

    /**
     * Gets the MimeType of an [Uri]
     */
    fun getMimeType(uri: Uri): String = uri.getMimeType(context)

    /**
     * Gets the real path of an [Uri]
     */
    fun getRealPath(uri: Uri): String? = uri.getRealPathFromURI(context)

    fun getFileSize(uri: Uri) = uri.getFileSize(context)

    fun getInputStream(uri: Uri) = uri.getInputStream(context)
}