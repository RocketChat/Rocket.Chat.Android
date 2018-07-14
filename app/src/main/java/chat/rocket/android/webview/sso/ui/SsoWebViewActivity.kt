package chat.rocket.android.webview.sso.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import chat.rocket.android.R
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.app_bar.*

fun Context.ssoWebViewIntent(webPageUrl: String, casToken: String): Intent {
    return Intent(this, SsoWebViewActivity::class.java).apply {
        putExtra(INTENT_WEB_PAGE_URL, webPageUrl)
        putExtra(INTENT_SSO_TOKEN, casToken)
    }
}

private const val INTENT_WEB_PAGE_URL = "web_page_url"
const val INTENT_SSO_TOKEN = "cas_token"

/**
 * This class is responsible to handle the authentication thought single sign-on protocol (CAS and SAML).
 */
class SsoWebViewActivity : AppCompatActivity() {
    private lateinit var webPageUrl: String
    private lateinit var casToken: String
    private var isWebViewSetUp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webPageUrl = intent.getStringExtra(INTENT_WEB_PAGE_URL)
        requireNotNull(webPageUrl) { "no web_page_url provided in Intent extras" }

        casToken = intent.getStringExtra(INTENT_SSO_TOKEN)
        requireNotNull(casToken) { "no cas_token provided in Intent extras" }

        // Ensures that the cookies is always removed when opening the webview.
        CookieManager.getInstance().removeAllCookies(null)
        setupToolbar()
    }

    override fun onResume() {
        super.onResume()
        if (!isWebViewSetUp) {
            setupWebView()
            isWebViewSetUp = true
        }
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            web_view.goBack()
        } else {
            closeView()
        }
    }

    private fun setupToolbar() {
        toolbar.title = getString(R.string.title_authentication)
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener { closeView() }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                // The user may have already been logged in the SSO, so check if the URL contains
                // the "ticket" or "validate" word (that means the user is successful authenticated
                // and we don't need to wait until the page is fully loaded).
                if (url.contains("ticket") || url.contains("validate")) {
                    closeView(Activity.RESULT_OK)
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (url.contains("ticket") || url.contains("validate")) {
                    closeView(Activity.RESULT_OK)
                } else {
                    view_loading.hide()
                }
            }
        }
        web_view.loadUrl(webPageUrl)
    }

    private fun closeView(activityResult: Int = Activity.RESULT_CANCELED) {
        setResult(activityResult, Intent().putExtra(INTENT_SSO_TOKEN, casToken))
        finish()
        overridePendingTransition(R.anim.hold, R.anim.slide_down)
    }
}