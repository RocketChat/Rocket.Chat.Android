package chat.rocket.android.util

fun String.removeTrailingSlash(): String {
    return if (isNotEmpty() && this[length - 1] == '/') {
        this.substring(0, length - 1)
    } else {
        this
    }
}

fun String.avatarUrl(
    avatar: String,
    isGroupOrChannel: Boolean = false,
    format: String = "jpeg"
): String {
    return if (isGroupOrChannel) {
        "${removeTrailingSlash()}/avatar/%23${avatar.removeTrailingSlash()}?format=$format"
    } else {
        "${removeTrailingSlash()}/avatar/${avatar.removeTrailingSlash()}?format=$format"
    }
}