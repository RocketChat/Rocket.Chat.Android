package chat.rocket.android.util.extensions

import android.util.Patterns

fun String.removeTrailingSlash(): String {
    return if (isNotEmpty() && this[length - 1] == '/') {
        this.substring(0, length - 1)
    } else {
        this
    }
}

fun String.sanitize(): String {
    val tmp = this.trim()
    return tmp.removeTrailingSlash()
}

fun String.avatarUrl(avatar: String, isGroupOrChannel: Boolean = false, format: String = "jpeg"): String {
    return if (isGroupOrChannel) {
        "${removeTrailingSlash()}/avatar/%23${avatar.removeTrailingSlash()}?format=$format"
    } else {
        "${removeTrailingSlash()}/avatar/${avatar.removeTrailingSlash()}?format=$format"
    }
}

fun String.serverLogoUrl(favicon: String) = "${removeTrailingSlash()}/$favicon"

fun String.casUrl(serverUrl: String, token: String) =
    "${removeTrailingSlash()}?service=${serverUrl.removeTrailingSlash()}/_cas/$token"

fun String.termsOfServiceUrl() = "${removeTrailingSlash()}/terms-of-service"

fun String.privacyPolicyUrl() = "${removeTrailingSlash()}/privacy-policy"

fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()