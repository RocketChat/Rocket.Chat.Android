package chat.rocket.android.util.extension

/**
 * This purely checks if an url link ends with a image format.
 */
fun String.isImage() = endsWith(".gif") || endsWith(".png") || endsWith(".jpg") || endsWith("jpeg")