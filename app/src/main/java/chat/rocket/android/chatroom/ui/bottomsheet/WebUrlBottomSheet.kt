package chat.rocket.android.chatroom.ui.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import chat.rocket.android.R
import chat.rocket.android.util.extensions.ui
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.webview_bottomsheet.*

private const val BUNDLE_WEB_PAGE_URL = "web_page_url"
private const val BUNDLE_CHATROOM_ID = "chatroom_id"

class WebUrlBottomSheet : BottomSheetDialogFragment() {

    private lateinit var webPageUrl: String
    private lateinit var chatRoomId: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        arguments?.run {
            webPageUrl = getString(BUNDLE_WEB_PAGE_URL, "")
            chatRoomId = getString(BUNDLE_CHATROOM_ID, "")
        } ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
        return inflater.inflate(R.layout.webview_bottomsheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupToolbar()
        setupWebView()
    }

    override fun onStart() {
        super.onStart()
        var bottomsheet: FrameLayout = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet)
        var behaviour = BottomSheetBehavior.from(bottomsheet)
        behaviour.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(p0: View, p1: Float) {
            }

            override fun onStateChanged(p0: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dialog.cancel()
                }
            }

        })
        behaviour.state = BottomSheetBehavior.STATE_HALF_EXPANDED
    }

    private fun setupToolbar() {
        toolbar.title = webPageUrl.replace("https://","")
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp)
        toolbar.setNavigationOnClickListener {
            dialog.cancel()
        }
    }

    override fun onResume() {
        super.onResume()
        setupWebView()
    }

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
                }
            }
        }
        web_view.loadUrl(webPageUrl)
    }

    companion object {
        fun newInstance(webPageUrl: String, chatRoomId: String) = WebUrlBottomSheet().apply {
            arguments = Bundle(2).apply {
                putString(BUNDLE_WEB_PAGE_URL, webPageUrl)
                putString(BUNDLE_CHATROOM_ID, chatRoomId)
            }
        }
    }
}