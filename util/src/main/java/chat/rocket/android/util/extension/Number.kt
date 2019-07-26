package chat.rocket.android.util.extension

fun Long.orZero(): Long {
    return if (this < 0) 0 else this
}