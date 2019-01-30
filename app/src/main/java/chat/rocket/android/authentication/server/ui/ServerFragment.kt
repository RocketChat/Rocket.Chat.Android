package chat.rocket.android.authentication.server.ui

import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ScrollView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.text.color
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.BuildConfig
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.server.presentation.ServerPresenter
import chat.rocket.android.authentication.server.presentation.ServerView
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.hintContent
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.isValidUrl
import chat.rocket.android.util.extensions.sanitize
import chat.rocket.android.util.extensions.setLightStatusBar
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import chat.rocket.common.util.ifNull
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.android.synthetic.main.fragment_authentication_server.*
import okhttp3.HttpUrl
import javax.inject.Inject

fun newInstance() = ServerFragment()

private const val DEEP_LINK_INFO = "DeepLinkInfo"

class ServerFragment : Fragment(), ServerView {
    @Inject
    lateinit var presenter: ServerPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private var deepLinkInfo: LoginDeepLinkInfo? = null
    private var protocol = "https://"
    private var isDomainAppended = false
    private var appendedText = ""
    private lateinit var serverUrlDisposable: Disposable
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (KeyboardHelper.isSoftKeyboardShown(scroll_view.rootView)) {
            scroll_view.fullScroll(ScrollView.FOCUS_DOWN)
            text_server_url.isCursorVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        deepLinkInfo = arguments?.getParcelable(DEEP_LINK_INFO)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_authentication_server)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scroll_view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        setupToolbar()
        setupSpinner()
        setupOnClickListener()
        subscribeEditText()

        deepLinkInfo?.let {
            it.url.toUri().host?.let { host -> text_server_url.hintContent = host }
            presenter.deepLink(it)
        }

        analyticsManager.logScreenView(ScreenViewEvent.Server)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scroll_view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        // Reset deep link info, so user can come back and log to another server...
        deepLinkInfo = null
        unsubscribeEditText()
    }

    private fun setupToolbar() {
        with(activity as AuthenticationActivity) {
            view?.let { setLightStatusBar(it) }
            toolbar.isVisible = false
        }
    }

    private fun setupSpinner() {
        context?.let {
            spinner_server_protocol.adapter = ArrayAdapter<String>(
                it,
                android.R.layout.simple_dropdown_item_1line, arrayOf("https://", "http://")
            )

            spinner_server_protocol.onItemSelectedListener =
                    object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            parent: AdapterView<*>?, view: View?, position: Int,
                            id: Long
                        ) {
                            when (position) {
                                0 -> protocol = "https://"
                                1 -> {
                                    protocol = "http://"
                                    showToast(R.string.msg_http_insecure, Toast.LENGTH_LONG)
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>?) {}
                    }

        }
    }

    private fun setupOnClickListener() =
        ui {
            button_connect.setOnClickListener {
                presenter.checkServer("$protocol${text_server_url.textContent.sanitize()}")
            }
        }

    override fun showInvalidServerUrlMessage() =
        showMessage(getString(R.string.msg_invalid_server_url))

    override fun enableButtonConnect() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_connect, ContextCompat.getColorStateList(it, R.color.colorAccent)
            )
            button_connect.isEnabled = true
        }
    }

    override fun disableButtonConnect() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_connect,
                ContextCompat.getColorStateList(it, R.color.colorAuthenticationButtonDisabled)
            )
            button_connect.isEnabled = false
        }
    }

    override fun showLoading() {
        ui {
            disableUserInput()
            view_loading.isVisible = true
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.isVisible = false
            enableUserInput()
        }
    }

    override fun showMessage(resId: Int) {
        ui {
            showToast(resId)
        }
    }

    override fun showMessage(message: String) {
        ui {
            showToast(message)
        }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun alertNotRecommendedVersion() {
        ui {
            hideLoading()
            showToast(
                getString(R.string.msg_ver_not_recommended, BuildConfig.RECOMMENDED_SERVER_VERSION)
            )
            performConnect()
        }
    }

    override fun blockAndAlertNotRequiredVersion() {
        ui {
            hideLoading()
            showToast(getString(R.string.msg_ver_not_minimum, BuildConfig.REQUIRED_SERVER_VERSION))
            // reset the deeplink info, so the user can log to another server...
            deepLinkInfo = null
        }
    }

    override fun versionOk() = performConnect()

    override fun errorCheckingServerVersion() {
        hideLoading()
        showMessage(R.string.msg_error_checking_server_version)
    }

    override fun errorInvalidProtocol() {
        hideLoading()
        showMessage(R.string.msg_invalid_server_protocol)
    }

    override fun updateServerUrl(url: HttpUrl) {
        ui {
            if (url.scheme() == "https") {
                spinner_server_protocol.setSelection(0)
            } else {
                spinner_server_protocol.setSelection(1)
            }

            protocol = "${url.scheme()}://"
            text_server_url.textContent = url.toString().removePrefix(protocol)
        }
    }

    private fun performConnect() {
        ui {
            deepLinkInfo?.let { loginDeepLinkInfo ->
                presenter.deepLink(loginDeepLinkInfo)
            }.ifNull {
                presenter.connect("$protocol${text_server_url.textContent.sanitize()}")
            }
        }
    }

    private fun subscribeEditText() {
        serverUrlDisposable = text_server_url.asObservable()
            .filter { it.isNotBlank() }
            .subscribe { processUserInput(it.toString()) }
    }

    private fun unsubscribeEditText() = serverUrlDisposable.dispose()

    private fun processUserInput(text: String) {
        if (text.last().toString() == "." && !isDomainAppended) {
            addDomain()
        } else if (isDomainAppended && text != appendedText) {
            removeDomain()
        }

        if ("$protocol$text".isValidUrl()) {
            enableButtonConnect()
        } else {
            disableButtonConnect()
        }
    }

    private fun addDomain() {
        val cursorPosition = text_server_url.length()
        text_server_url.append(SpannableStringBuilder()
            .color(R.color.colorAuthenticationSecondaryText) { append("rocket.chat") })
        text_server_url.setSelection(cursorPosition)
        appendedText = text_server_url.text.toString()
        isDomainAppended = true
    }

    private fun removeDomain() {
        text_server_url.setText(
            text_server_url.text.toString().substring(0, text_server_url.selectionEnd)
        )
        text_server_url.setSelection(text_server_url.length())
        isDomainAppended = false
    }

    private fun enableUserInput() {
        enableButtonConnect()
        text_server_url.isEnabled = true
    }

    private fun disableUserInput() {
        disableButtonConnect()
        text_server_url.isEnabled = false
    }
}
