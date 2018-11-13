package chat.rocket.android.util.extensions


inline fun CharSequence?.ifNotNullNorEmpty(block: (CharSequence) -> Unit) {
    if (this != null && this.isNotEmpty()) {
        block(this)
    }
}

fun CharSequence?.isNotNullNorEmpty(): Boolean = this != null && this.isNotEmpty()