package chat.rocket.android.webview.oauth.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.net.toUri
import chat.rocket.android.R
import chat.rocket.android.util.extensions.decodeUrl
import chat.rocket.android.util.extensions.toJsonObject
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.app_bar.*
import org.json.JSONObject

fun Context.oauthWebViewIntent(webPageUrl: String, state: String): Intent {
    return Intent(this, OauthWebViewActivity::class.java).apply {
        putExtra(INTENT_WEB_PAGE_URL, webPageUrl)
        putExtra(INTENT_STATE, state)
    }
}

private const val INTENT_WEB_PAGE_URL = "web_page_url"
private const val INTENT_STATE = "state"
private const val JSON_CREDENTIAL_TOKEN = "credentialToken"
private const val JSON_CREDENTIAL_SECRET = "credentialSecret"
const val INTENT_OAUTH_CREDENTIAL_TOKEN = "credential_token"
const val INTENT_OAUTH_CREDENTIAL_SECRET = "credential_secret"

// Shows a WebView to the user authenticate with its OAuth credential.
class OauthWebViewActivity : AppCompatActivity() {
    private lateinit var webPageUrl: String
    private lateinit var state: String
    private var isWebViewSetUp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webPageUrl = intent.getStringExtra(INTENT_WEB_PAGE_URL)
        requireNotNull(webPageUrl) { "no web_page_url provided in Intent extras" }

        state = intent.getStringExtra(INTENT_STATE)
        requireNotNull(state) { "no state provided in Intent extras" }

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
        with(toolbar) {
            title = getString(R.string.title_authentication)
            setNavigationIcon(R.drawable.ic_close_white_24dp)
            setNavigationOnClickListener { closeView() }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        with(web_view.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            // TODO Remove this workaround that is required to make Google OAuth to work. We should use Custom Tabs instead. See https://github.com/RocketChat/Rocket.Chat.Android/issues/968
            if (webPageUrl.contains("google")) {
                userAgentString =
                        "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/43.0.2357.65 Mobile Safari/535.19"
            }
        }
        web_view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                if (url.contains(JSON_CREDENTIAL_TOKEN) && url.contains(JSON_CREDENTIAL_SECRET)) {
                    if (isStateValid(url)) {
                        val jsonResult = url.decodeUrl()
                            .substringAfter("#")
                            .toJsonObject()
                        val credentialToken = getCredentialToken(jsonResult)
                        val credentialSecret = getCredentialSecret(jsonResult)
                        if (credentialToken.isNotEmpty() && credentialSecret.isNotEmpty()) {
                            closeView(Activity.RESULT_OK, credentialToken, credentialSecret)
                        }
                    }
                }
                view_loading.hide()
            }
        }
        web_view.loadUrl(webPageUrl)
    }

    // If the states matches, then try to get the code, otherwise the request was created by a third party and the process should be aborted.
    private fun isStateValid(url: String): Boolean =
        url.substringBefore("#").toUri().getQueryParameter(INTENT_STATE) == state

    private fun getCredentialToken(json: JSONObject): String =
        json.optString(JSON_CREDENTIAL_TOKEN)

    private fun getCredentialSecret(json: JSONObject): String =
        json.optString(JSON_CREDENTIAL_SECRET)

    private fun closeView(
        activityResult: Int = Activity.RESULT_CANCELED,
        credentialToken: String? = null,
        credentialSecret: String? = null
    ) {
        setResult(
            activityResult,
            Intent().putExtra(INTENT_OAUTH_CREDENTIAL_TOKEN, credentialToken).putExtra(
                INTENT_OAUTH_CREDENTIAL_SECRET,
                credentialSecret
            )
        )
        finish()
        overridePendingTransition(R.anim.hold, R.anim.slide_down)
    }
}