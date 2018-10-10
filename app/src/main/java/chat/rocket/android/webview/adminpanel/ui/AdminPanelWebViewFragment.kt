package chat.rocket.android.webview.adminpanel.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.ui
import kotlinx.android.synthetic.main.fragment_admin_panel_web_view.*

private const val BUNDLE_WEB_PAGE_URL = "web_page_url"
private const val BUNDLE_USER_TOKEN = "user_token"

class AdminPanelWebViewFragment : Fragment() {
    private lateinit var webPageUrl: String
    private lateinit var userToken: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments
        if (bundle != null) {
            webPageUrl = bundle.getString(BUNDLE_WEB_PAGE_URL)
            userToken = bundle.getString(BUNDLE_USER_TOKEN)
        } else {
            requireNotNull(bundle) { "no arguments supplied when the fragment was instantiated" }
        }
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
                ui { _ ->
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