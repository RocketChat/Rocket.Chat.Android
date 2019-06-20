package chat.rocket.android.util.extensions

import android.graphics.Color
import android.util.Patterns
import chat.rocket.common.model.Token
import okhttp3.HttpUrl
import timber.log.Timber

fun String.removeTrailingSlash(): String {
    var removed = this
    while (removed.isNotEmpty() && removed[removed.length - 1] == '/') {
        removed = removed.substring(0, removed.length - 1)
    }
    return removed
}

fun String.sanitize(): String {
    val tmp = this.trim()
    return tmp.removeTrailingSlash()
}

fun String.avatarUrl(
    avatar: String,
    userId: String?,
    token: String?,
    isGroupOrChannel: Boolean = false,
    format: String = "jpeg"
): String {
    return if (isGroupOrChannel) {
        "${removeTrailingSlash()}/avatar/%23${avatar.removeTrailingSlash()}?format=$format&rc_uid=$userId&rc_token=$token"
    } else {
        "${removeTrailingSlash()}/avatar/${avatar.removeTrailingSlash()}?format=$format&rc_uid=$userId&rc_token=$token"
    }
}

fun String.fileUrl(path: String, token: Token): String {
    return (this + path + "?rc_uid=${token.userId}" + "&rc_token=${token.authToken}").safeUrl()
}

fun String.safeUrl(): String {
    return this.replace(" ", "%20").replace("\\", "")
}

fun String.serverLogoUrl(favicon: String) = "${removeTrailingSlash()}/$favicon"

fun String.casUrl(serverUrl: String, casToken: String) =
    "${removeTrailingSlash()}?service=${serverUrl.removeTrailingSlash()}/_cas/$casToken"

fun String.samlUrl(provider: String, samlToken: String) =
    "${removeTrailingSlash()}/_saml/authorize/$provider/$samlToken"

fun String.termsOfServiceUrl() = "${removeTrailingSlash()}/terms-of-service"

fun String.privacyPolicyUrl() = "${removeTrailingSlash()}/privacy-policy"

fun String.adminPanelUrl() = "${removeTrailingSlash()}/admin/info?layout=embedded"

fun String.isValidUrl(): Boolean = Patterns.WEB_URL.matcher(this).matches()

fun String.parseColor(): Int {
    return try {
        Color.parseColor(this)
    } catch (exception: IllegalArgumentException) {
        // Log the exception and get the white color.
        Timber.e(exception)
        Color.parseColor("white")
    }
}

fun String.userId(userId: String?): String? {
    return userId?.let { this.replace(it, "") }
}

fun String.lowercaseUrl(): String? = HttpUrl.parse(this)?.run {
    newBuilder().scheme(scheme().toLowerCase()).build().toString()
}

fun String?.isNotNullNorEmpty(): Boolean = this != null && this.isNotEmpty()

fun String?.isNotNullNorBlank(): Boolean = this != null && this.isNotBlank()

inline fun String?.ifNotNullNotEmpty(block: (String) -> Unit) {
    if (this != null && this.isNotEmpty()) {
        block(this)
    }
}