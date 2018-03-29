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
import chat.rocket.android.customtab.CustomTab
import chat.rocket.android.customtab.WebViewFallback
import chat.rocket.android.dagger.DaggerAppComponent
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.room.weblink.WebLinkDao
import chat.rocket.android.room.weblink.WebLinkEntity
import chat.rocket.android.util.TimberLogger
import com.r0adkll.slidr.Slidr
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.activity_web_view.*
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject

fun Context.webViewIntent(webPageUrl: String, title: String = "Web Chat"): Intent {
    return Intent(this, WebViewActivity::class.java).apply {
        putExtra(WebViewActivity.INTENT_WEB_PAGE_URL, webPageUrl)
        putExtra(WebViewActivity.INTENT_WEB_PAGE_TITLE, title)
    }
}

//Simple WebView to load URL.
class WebViewActivity : AppCompatActivity() {
    companion object {
        const val INTENT_WEB_PAGE_URL = "web_page_url"
        const val INTENT_WEB_PAGE_TITLE = "web_page_title"
    }

    @Inject
    lateinit var webLinkDao: WebLinkDao

    private lateinit var webPageUrl: String
    private lateinit var webPageTitle: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_web_view)
        DaggerAppComponent.builder().application(RocketChatApplication.application).build().inject(this)

        webPageUrl = intent.getStringExtra(INTENT_WEB_PAGE_URL)
        webPageTitle = intent.getStringExtra(INTENT_WEB_PAGE_TITLE)

        requireNotNull(webPageUrl) { "no web_page_url provided in Intent extras" }

        setupToolbar()

        Slidr.attach(this)
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
        toolbar.inflateMenu(R.menu.web_links_bookmark)
        toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_bookmark) {
                performBookmarkAction()
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun performBookmarkAction() {
        launch {
            val webLink: WebLinkEntity? = webLinkDao.getWebLink(webPageUrl)

            if (webLink != null) {
                webLinkDao.deleteWebLink(webLink)
                showToast(this@WebViewActivity.resources.getString(R.string.removed_bookmark))
            } else {
                webLinkDao.insertWebLink(WebLinkEntity(link = webPageUrl))
                showToast(this@WebViewActivity.resources.getString(R.string.added_bookmark))
            }
        }
    }

    private fun truncateString(string: String): String {
        return if (string.length >= 25)
            string.substring(0, 24) + "..."
        else
            string
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view_loading.hide()
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return isNewUrl(request?.url.toString())
            }
        }
        web_view.loadUrl(webPageUrl)
    }

    private fun isNewUrl(url: String): Boolean {
        var redirectedUrl: String? = ""

        runBlocking {
            redirectedUrl = getRedirectedUrl(webPageUrl).await()
        }

        return if (url.equals(webPageUrl, true)
                || (redirectedUrl.equals(url, true) && !redirectedUrl.equals(webPageUrl))
                || webPageUrl.contains(url, true)
                || UrlHelper.removeTrailingSlash(url).equals(UrlHelper.removeTrailingSlash(webPageUrl), true)) {
            false
        } else {
            CustomTab.openCustomTab(this@WebViewActivity, url, WebViewFallback())
            true
        }
    }

    fun showToast(string: String) {
        Observable.just(string)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    Toast.makeText(RocketChatApplication.application.applicationContext, string, Toast.LENGTH_SHORT).show()
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