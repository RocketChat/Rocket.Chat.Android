package chat.rocket.android.authentication.domain.model

import android.annotation.SuppressLint
import android.content.Intent
import androidx.core.net.toUri
import android.os.Parcelable
import chat.rocket.android.util.extensions.decodeUrl
import chat.rocket.android.util.extensions.toJsonObject
import kotlinx.android.parcel.Parcelize
import timber.log.Timber

private const val JSON_CREDENTIAL_TOKEN = "credentialToken"
private const val JSON_CREDENTIAL_SECRET = "credentialSecret"
private const val OAUTH_STATE = "state"

@SuppressLint("ParcelCreator")
@Parcelize
data class OauthDeepLinkInfo(
        val state: String,
        val credentialToken: String,
        val credentialSecret: String
) : Parcelable

fun Intent.getOauthDeepLinkInfo(): OauthDeepLinkInfo? {
    val uri = data
    return if (action == Intent.ACTION_VIEW && uri != null && isOauthDeepLink(uri.toString())) {
        val jsonResult = uri
                .toString()
                .decodeUrl()
                .substringAfter("#")
                .toJsonObject()
        val credentialToken = jsonResult.optString(JSON_CREDENTIAL_TOKEN)
        val credentialSecret = jsonResult.optString(JSON_CREDENTIAL_SECRET)
        val oauthState = uri
                .toString()
                .substringBefore("#")
                .toUri()
                .getQueryParameter(OAUTH_STATE)
        try {
            OauthDeepLinkInfo(oauthState, credentialToken, credentialSecret)
        } catch (ex: Exception) {
            Timber.d(ex, "Error parsing oauth deeplink")
            null
        }
    } else null
}

private fun isOauthDeepLink(uri: String): Boolean {
    if (uri.contains(JSON_CREDENTIAL_TOKEN) && uri.contains(JSON_CREDENTIAL_SECRET)) {
        return true
    }
    return false
}