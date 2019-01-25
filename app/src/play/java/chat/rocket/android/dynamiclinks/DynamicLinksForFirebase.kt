package chat.rocket.android.dynamiclinks

import android.content.Context
import android.content.Intent
import android.net.Uri
import chat.rocket.android.util.TimberLogger
import chat.rocket.android.R
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.google.firebase.dynamiclinks.ShortDynamicLink
import javax.inject.Inject

import kotlinx.coroutines.experimental.runBlocking

// DEBUG
import android.widget.Toast

class DynamicLinksForFirebase @Inject constructor(private val context: Context) :
        DynamicLinks {

    private var deepLink: Uri? = null
    private var newDeepLink: String? = null

    override fun getDynamicLink(intent: Intent) : Uri? {

        runBlocking {

            FirebaseDynamicLinks.getInstance()
                    .getDynamicLink(intent)
                    .addOnSuccessListener { pendingDynamicLinkData ->
                        if (pendingDynamicLinkData != null) {
                            deepLink = pendingDynamicLinkData.link
                        }

                        TimberLogger.debug("DeepLink:" + deepLink.toString())
                    }
                    .addOnFailureListener { e -> TimberLogger.debug("getDynamicLink:onFailure : $e") }
        }
        return deepLink
    }

    override fun createDynamicLink(username: String, server: String ) : String? {

        runBlocking {

            FirebaseDynamicLinks.getInstance().createDynamicLink()
                    .setLink(Uri.parse("$server/direct/$username"))
                    .setDomainUriPrefix("https://" + context.getString(R.string.widechat_deeplink_host))
                    .setAndroidParameters(
                            DynamicLink.AndroidParameters.Builder(context.getString(R.string.widechat_package_name)).build())
                    .setSocialMetaTagParameters(
                            DynamicLink.SocialMetaTagParameters.Builder()
                                    .setTitle(username)
                                    .setDescription("Chat with $username on " + context.getString(R.string.widechat_server_url))
                                    .build())
                    .buildShortDynamicLink(ShortDynamicLink.Suffix.SHORT)
                    .addOnSuccessListener { result ->
                        newDeepLink = result.shortLink.toString()
                        Toast.makeText(context, newDeepLink, Toast.LENGTH_SHORT).show()

                    }.addOnFailureListener {
                        // Error
                        Toast.makeText(context, "Error dynamic link", Toast.LENGTH_SHORT).show()
                    }
        }
        return newDeepLink
    }
}


