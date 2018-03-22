package chat.rocket.android.customtab

import android.content.Context
import android.content.Intent
import android.net.Uri

import chat.rocket.android.webview.weblink.ui.WebViewActivity

/**
 * A Fallback that opens a Webview when Custom Tabs is not available
 */
class WebViewFallback : CustomTab.CustomTabFallback {
    override fun openUri(context: Context, uri: Uri) {
        val intent = Intent(context, WebViewActivity::class.java)
        intent.putExtra(WebViewActivity.INTENT_WEB_PAGE_URL, uri.toString())
        context.startActivity(intent)
    }
}
