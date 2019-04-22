package chat.rocket.android.webview.adminpanel.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.ui
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_admin_panel_web_view.*
import javax.inject.Inject

private const val BUNDLE_WEB_PAGE_URL = "web_page_url"
private const val BUNDLE_USER_TOKEN = "user_token"

class AdminPanelWebViewFragment : DaggerFragment() {
    private lateinit var webPageUrl: String
    private lateinit var userToken: String
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.run {
            webPageUrl = getString(BUNDLE_WEB_PAGE_URL, "")
            userToken = getString(BUNDLE_USER_TOKEN, "")
        } ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_admin_panel_web_view)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupWebView()

        analyticsManager.logOpenAdmin()
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)?.supportActionBar?.title =
                getString(R.string.title_admin_panel)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        with(web_view.settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
        }

        web_view.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                ui {
                    view_loading.hide()
                    web_view.evaluateJavascript("Meteor.loginWithToken('$userToken', function() { })") {}
                }
            }
        }
        web_view.loadUrl(webPageUrl)
    }

    companion object {
        fun newInstance(webPageUrl: String, userToken: String) = AdminPanelWebViewFragment().apply {
            arguments = Bundle(2).apply {
                putString(BUNDLE_WEB_PAGE_URL, webPageUrl)
                putString(BUNDLE_USER_TOKEN, userToken)
            }
        }
    }
}