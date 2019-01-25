package chat.rocket.android.dynamiclinks

import android.content.Context
import android.content.Intent
import android.net.Uri
import javax.inject.Inject

class DynamicLinksForFirebase @Inject constructor(private val context: Context) : DynamicLinks {

    override fun getDynamicLink(intent: Intent) : Uri? { return null}

    override fun createDynamicLink(username: String, server: String) : String? { return null }
}
