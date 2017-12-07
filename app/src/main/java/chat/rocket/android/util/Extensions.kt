package chat.rocket.android.util

fun String.ifEmpty(value: String): String {
    if (isEmpty()) {
        return value
    }
    return this
}