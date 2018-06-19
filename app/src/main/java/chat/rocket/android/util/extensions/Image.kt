package chat.rocket.android.util.extensions

import android.graphics.Bitmap

fun Bitmap.getCompressFormat(mimeType: String): Bitmap.CompressFormat {
    return when {
        mimeType.contains("jpeg") -> Bitmap.CompressFormat.JPEG
        mimeType.contains("webp") -> Bitmap.CompressFormat.WEBP
        else -> Bitmap.CompressFormat.PNG
    }
}