package chat.rocket.android.webview.weblink.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.app.RocketChatApplication
import chat.rocket.android.customtab.CustomTabHelper
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.util.TimberLogger
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import java.net.HttpURLConnection
import java.net.URL

fun Context.webViewIntent(webPageUrl: String, title: String = "Web Chat"): Intent {
    return Intent(this, WebViewActivity::class.java).apply {
        putExtra(INTENT_WEB_PAGE_URL, webPageUrl)
        putExtra(INTENT_WEB_PAGE_TITLE, title)
    }
}

private const val INTENT_WEB_PAGE_URL = "web_page_url"
private const val INTENT_WEB_PAGE_TITLE = "web_page_title"

//Simple WebView to load URL.
class WebViewActivity : AppCompatActivity() {

    private lateinit var webPageUrl: String
    private lateinit var webPageTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)

        webPageUrl = intent.getStringExtra(INTENT_WEB_PAGE_URL)
        webPageTitle = intent.getStringExtra(INTENT_WEB_PAGE_TITLE)

        requireNotNull(webPageUrl) { "no web_page_url provided in Intent extras" }

        setupToolbar()
    }

    override fun onResume() {
        super.onResume()
        setupWebView()
    }

    override fun onBackPressed() {
        if (web_view.canGoBack()) {
            web_view.goBack()
        } else {
            finishActivity()
        }
    }

    private fun setupToolbar() {
        toolbar.title = truncateString(webPageTitle)
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            finishActivity()
        }
    }

    private fun truncateString(string: String): String {
        if (string.length >= 25)
            return string.substring(0, 24) + "..."
        else
            return string
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        web_view.settings.javaScriptEnabled = true
        web_view.settings.setSupportZoom(true)
        web_view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view_loading.hide()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url.toString()
                var redirectedUrl: String? = ""

                runBlocking {
                    redirectedUrl = getRedirectedUrl(webPageUrl).await()
                }

                if (url.equals(webPageUrl, true)
                        || (redirectedUrl.equals(url, true) && !redirectedUrl.equals(webPageUrl))
                        || webPageUrl.contains(url, true)
                        || UrlHelper.removeTrailingSlash(url).equals(UrlHelper.removeTrailingSlash(webPageUrl), true)) {
                    return false
                } else {
                    CustomTabHelper.openCustomTab(this@WebViewActivity, Uri.parse(url))
                    return true
                }
            }
        }
        web_view.loadUrl(webPageUrl)
    }

    fun showToast(string: String) {
        Observable.just(string)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(RocketChatApplication.application.applicationContext, string, Toast.LENGTH_SHORT).show();
                })
    }

    private fun getRedirectedUrl(string: String): Deferred<String> = async {
        val url = URL(string)
        val urlConnection: HttpURLConnection = url.openConnection() as HttpURLConnection
        urlConnection.instanceFollowRedirects = false
        var redirectedUrl = string
        try {
            if (urlConnection.headerFields.containsKey("Location"))
                redirectedUrl = urlConnection.getHeaderField("Location").toString()
        } catch (e: Exception) {
            TimberLogger.debug(e.toString())
        }
        return@async redirectedUrl
    }

    private fun finishActivity() {
        super.onBackPressed()
        overridePendingTransition(R.anim.hold, R.anim.slide_down)
    }
}