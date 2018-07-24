package chat.rocket.android.authentication.domain.model

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

@SuppressLint("ParcelCreator")
@Parcelize
data class LoginDeepLinkInfo(
    val url: String,
    val userId: String?,
    val token: String?
) : Parcelable

fun Intent.getLoginDeepLinkInfo(): LoginDeepLinkInfo? {
    val uri = data
    return if (action == Intent.ACTION_VIEW && uri != null && uri.isAuthenticationDeepLink()) {
        val host = uri.getQueryParameter("host")
        val url = if (host.startsWith("http")) host else "https://$host"
        val userId = uri.getQueryParameter("userId")
        val token = uri.getQueryParameter("token")
        try {
            LoginDeepLinkInfo(url, userId, token)
        } catch (ex: Exception) {
            Timber.d(ex, "Error parsing login deeplink")
            null
        }
    } else null
}

private inline fun Uri.isAuthenticationDeepLink(): Boolean {
    if (host == "auth")
        return true
    else if (host == "go.rocket.chat" && path == "/auth")
        return true
    return false
}