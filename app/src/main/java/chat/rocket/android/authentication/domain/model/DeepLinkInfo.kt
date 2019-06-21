package chat.rocket.android.authentication.domain.model

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

private const val DEFAULT_SERVER_HOST = "open.rocket.chat"

// see https://rocket.chat/docs/developer-guides/deeplink/ for documentation

@SuppressLint("ParcelCreator")
@Parcelize
data class DeepLinkInfo(
    val url: String,
    val userId: String?,
    val token: String?,
    val rid: String?,
    val roomType: String?,
    val roomName: String?
) : Parcelable

fun Uri.getDeepLinkInfo(): DeepLinkInfo? {
    return if (isAuthenticationDeepLink()) {
        val host = getQueryParameter("host")
        val url = if (host.startsWith("http")) host else "https://$host"
        val userId = getQueryParameter("userId")
        val token = getQueryParameter("token")
        try {
            DeepLinkInfo(url, userId, token, null, null, null)
        } catch (ex: Exception) {
            Timber.d(ex, "Error parsing auth deeplink")
            null
        }
    } else if (isCustomSchemeRoomLink()) {
        val hostValue = getQueryParameter("host")
        val url = if (hostValue.startsWith("http")) hostValue else "https://$hostValue"
        val rid = getQueryParameter("rid")
        val pathValue = getQueryParameter("path")
        val pathSplit = pathValue.split("/")
        val roomType = pathSplit[0]
        val roomName = pathSplit[1]
        try {
            DeepLinkInfo(url, null, null, rid, roomType, roomName)
        } catch (ex: Exception) {
            Timber.d(ex, "Error parsing custom scheme room link")
            null
        }
    } else if (isWebSchemeRoomLink()) {
        val url = "https://$host"
        val pathSplit = path.split("/")
        val roomType = pathSplit[1]
        val roomName = pathSplit[2]
        try {
            DeepLinkInfo(url, null, null, null, roomType, roomName)
        } catch (ex: Exception) {
            Timber.d(ex, "Error parsing login deeplink")
            null
        }
    } else null
}

fun Intent.isSupportedLink(): Boolean {
    return (action == Intent.ACTION_VIEW && data != null &&
            (data.isDynamicLink() || data.isAuthenticationDeepLink() ||
                    data.isCustomSchemeRoomLink() || data.isWebSchemeRoomLink()))
}

fun Uri.isDynamicLink(): Boolean {
    return (host != null && host.contains("page.link", ignoreCase = true))
}

// Authentication deep link defined here: https://rocket.chat/docs/developer-guides/deeplink/#authentication
private inline fun Uri.isAuthenticationDeepLink(): Boolean {
    if (host == "auth")
        return true
    else if (host == DEFAULT_SERVER_HOST && path == "/auth")
        return true
    return false
}

// Custom scheme room deep link defined here: https://rocket.chat/docs/developer-guides/deeplink/#channel--group--dm
private inline fun Uri.isCustomSchemeRoomLink(): Boolean {
    if (scheme.startsWith("rocketchat") &&
            host == "room")
        return true
    return false
}

// http(s) scheme deep link not yet documented. Ex: https://open.rocket.chat/direct/testuser1
private inline fun Uri.isWebSchemeRoomLink(): Boolean {
    val roomType = path.split("/")[1]
    if (scheme.startsWith("http") &&
            (roomType == "channel" || roomType == "group" || roomType == "direct"))
        return true
    return false
}