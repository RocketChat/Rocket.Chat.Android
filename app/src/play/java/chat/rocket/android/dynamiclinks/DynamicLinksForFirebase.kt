package chat.rocket.android.dynamiclinks

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import chat.rocket.android.util.TimberLogger
import chat.rocket.android.R
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import javax.inject.Inject
import timber.log.Timber

class DynamicLinksForFirebase @Inject constructor(private var context: Context) :
        DynamicLinks {
    private var deepLink: Uri? = null
    private var newDeepLink: String? = null

    override fun getDynamicLink(intent: Intent, deepLinkCallback: (Uri?) -> Unit?) {
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener { pendingDynamicLinkData ->
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.link
                    }
                    deepLinkCallback(deepLink)
                }
                .addOnFailureListener { e -> TimberLogger.debug("getDynamicLink:onFailure : $e") }
    }

    override fun createDynamicLink(username: String, server: String, deepLinkCallback: (String?) -> Unit? ) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink("$server/direct/$username".toUri())
                .setDomainUriPrefix("https://" + context.getString(R.string.dynamiclink_host))
                .setAndroidParameters(
                        DynamicLink.AndroidParameters.Builder(context.getString(R.string.app_package_name)).build())
                .setSocialMetaTagParameters(
                        DynamicLink.SocialMetaTagParameters.Builder()
                                .setTitle(username)
                                .setDescription(context.getString(R.string.msg_dynamiclink_description, username, server))
                                .build())
                .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                .addOnSuccessListener { result ->
                    newDeepLink = result.shortLink.toString()
                    Timber.d("DynamicLink created: " + newDeepLink)
                    deepLinkCallback(newDeepLink)
                }.addOnFailureListener {
                    // Error
                    Timber.d("Error creating dynamicLink.")
                    deepLinkCallback(null)
                }
    }
}
