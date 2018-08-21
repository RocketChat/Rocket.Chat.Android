package chat.rocket.android.util

inline fun CharSequence?.isNotNullNorEmpty(block: (CharSequence) -> Unit) {
    if (this != null && this.isNotEmpty()) {
        block(this)
    }
}