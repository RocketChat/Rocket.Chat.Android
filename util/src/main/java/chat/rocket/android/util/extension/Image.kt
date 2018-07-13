package chat.rocket.android.util.extension

import android.graphics.Bitmap
import kotlinx.coroutines.experimental.DefaultDispatcher
import kotlinx.coroutines.experimental.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Compress a [Bitmap] image.
 *
 * @param mimeType The MimeType of what the compressed image should be.
 * @return An [InputStream] of a compressed image, otherwise null if the compression couldn't be done.
 */
suspend fun Bitmap.compressImageAndGetInputStream(mimeType: String): InputStream? {
    var inputStream: InputStream? = null

    withContext(DefaultDispatcher) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // TODO: Add an option the the app to the user be able to select the quality of the compressed image
        val isCompressed =
            this.compress(mimeType.getCompressFormat(), 70, byteArrayOutputStream)
        if (isCompressed) {
            inputStream = ByteArrayInputStream(byteArrayOutputStream.toByteArray())
        }
    }

    return inputStream
}

/**
 * Compress a [Bitmap] image.
 *
 * @param mimeType The MimeType of what the compressed image should be.
 * @return An [ByteArray] of a compressed image, otherwise null if the compression couldn't be done.
 */
suspend fun Bitmap.compressImageAndGetByteArray(mimeType: String): ByteArray? {
    var byteArray: ByteArray? = null

    withContext(DefaultDispatcher) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        // TODO: Add an option the the app to the user be able to select the quality of the compressed image
        val isCompressed =
            this.compress(mimeType.getCompressFormat(), 70, byteArrayOutputStream)
        if (isCompressed) {
            byteArray = byteArrayOutputStream.toByteArray()
        }
    }

    return byteArray
}

/**
 * Gets the [Bitmap.CompressFormat] based on the image MimeType.
 * Note: Supported formats are: PNG, JPEG and WEBP.
 */
fun String.getCompressFormat(): Bitmap.CompressFormat {
    return when {
        this.contains("jpeg") -> Bitmap.CompressFormat.JPEG
        this.contains("webp") -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.PNG
    }
}