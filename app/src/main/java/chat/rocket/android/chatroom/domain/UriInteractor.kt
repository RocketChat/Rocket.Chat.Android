package chat.rocket.android.chatroom.domain

import android.content.Context
import android.net.Uri
import chat.rocket.android.util.extensions.getFileName
import chat.rocket.android.util.extensions.getMimeType
import chat.rocket.android.util.extensions.getRealPathFromURI
import okio.Okio
import java.io.File
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

    /**
     * Save the contents of an [Uri] to a temp file named after uri.getFileName()
     */
    fun tempFile(uri: Uri): File? {
        try {
            val outputDir = context.cacheDir // context being the Activity pointer
            val outputFile = File(outputDir, uri.getFileName(context))
            val from = context.contentResolver.openInputStream(uri)

            Okio.source(from).use { a ->
                Okio.buffer(Okio.sink(outputFile)).use{ b ->
                    b.writeAll(a)
                    b.close()
                }
                a.close()
            }
            return outputFile
        } catch (ex: Exception) {
            ex.printStackTrace()
            return null
        }
    }
}