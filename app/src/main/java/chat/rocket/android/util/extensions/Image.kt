package chat.rocket.android.util.extensions

import android.graphics.Bitmap

fun Bitmap.getCompressFormat(mimeType: String): Bitmap.CompressFormat {
    var compressFormat = Bitmap.CompressFormat.PNG
    when {
        mimeType.contains("jpeg") -> {
            compressFormat = Bitmap.CompressFormat.JPEG
        }
        mimeType.contains("png") -> {
            compressFormat = Bitmap.CompressFormat.PNG
        }
        mimeType.contains("webp") -> {
            compressFormat = Bitmap.CompressFormat.WEBP
        }
    }
    return compressFormat
}