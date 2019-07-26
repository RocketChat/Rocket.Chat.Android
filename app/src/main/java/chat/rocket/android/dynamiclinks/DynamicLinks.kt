package chat.rocket.android.dynamiclinks

import android.content.Intent
import android.net.Uri

interface DynamicLinks {

    fun getDynamicLink(intent: Intent, deepLinkCallback: (Uri?) -> Unit? )

    fun createDynamicLink(username: String?, server: String, deepLinkCallback: (String?) -> Unit?)
}
