package chat.rocket.android.util.extensions

import android.util.Patterns

fun String.removeTrailingSlash(): String {
    return if (isNotEmpty() && this[length - 1] == '/') {
        this.replace("/+$", "")
    } else {
        this
    }
}

fun String.avatarUrl(avatar: String, format: String = "jpeg") =
    "${removeTrailingSlash()}/avatar/${avatar.removeTrailingSlash()}?format=$format"

fun String.serverLogoUrl(favicon: String) = "${removeTrailingSlash()}/$favicon"

fun String.casUrl(serverUrl: String, token: String) =
    "${removeTrailingSlash()}?service=${serverUrl.removeTrailingSlash()}/_cas/$token"

fun String.termsOfServiceUrl() = "${removeTrailingSlash()}/terms-of-service"

fun String.privacyPolicyUrl() = "${removeTrailingSlash()}/privacy-policy"

fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()