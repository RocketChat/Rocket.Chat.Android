package chat.rocket.android.dynamiclinks

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import chat.rocket.android.R
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import timber.log.Timber
import javax.inject.Inject

class DynamicLinksForFirebase @Inject constructor(private var context: Context) : DynamicLinks {
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
            .addOnFailureListener { Timber.e("getDynamicLink:onFailure : $it") }
    }

    override fun createDynamicLink(
        username: String?,
        server: String,
        deepLinkCallback: (String?) -> Unit?
    ) {
        FirebaseDynamicLinks.getInstance().createDynamicLink()
            .setLink("$server/direct/$username".toUri())
            .setDomainUriPrefix("https://" + context.getString(R.string.dynamic_link_host_url))
            .setAndroidParameters(
                DynamicLink.AndroidParameters.Builder(context.packageName).build()
            )
            .setSocialMetaTagParameters(
                DynamicLink.SocialMetaTagParameters.Builder()
                    .setTitle(username)
                    .setDescription(
                        context.getString(
                            R.string.msg_dynamic_link_description,
                            username,
                            server
                        )
                    )
                    .build()
            )
            .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
            .addOnSuccessListener { result ->
                Timber.i("DynamicLink created: $newDeepLink")
                newDeepLink = result.shortLink.toString()
                deepLinkCallback(newDeepLink)
            }.addOnFailureListener {
                Timber.e("Error creating dynamicLink.")
                deepLinkCallback(null)
            }
    }
}
