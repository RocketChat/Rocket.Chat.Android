package chat.rocket.android.authentication.server.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.server.presentation.ServerPresenter
import chat.rocket.android.authentication.server.presentation.ServerView
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.util.extensions.*
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_server.*
import javax.inject.Inject

class ServerFragment : Fragment(), ServerView {
    @Inject lateinit var presenter: ServerPresenter
    private var deepLinkInfo: LoginDeepLinkInfo? = null
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        text_server_url.isCursorVisible = KeyboardHelper.isSoftKeyboardShown(relative_layout.rootView)
    }

    companion object {
        private const val DEEP_LINK_INFO = "DeepLinkInfo"

        fun newInstance(deepLinkInfo: LoginDeepLinkInfo?) = ServerFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DEEP_LINK_INFO, deepLinkInfo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        deepLinkInfo = arguments?.getParcelable(DEEP_LINK_INFO)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        container?.inflate(R.layout.fragment_authentication_server)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        relative_layout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        setupOnClickListener()

        deepLinkInfo?.let {
            presenter.deepLink(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        relative_layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
    }

    override fun showInvalidServerUrlMessage() = showMessage(getString(R.string.msg_invalid_server_url))

    override fun showLoading() {
        ui {
            enableUserInput(false)
            view_loading.setVisible(true)
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.setVisible(false)
            enableUserInput(true)
        }
    }

    override fun showMessage(resId: Int){
        ui {
            showToast(resId)
        }
    }

    override fun showMessage(message: String) {
        ui {
            showToast(message)
        }
    }

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    private fun enableUserInput(value: Boolean) {
        button_connect.isEnabled = value
        text_server_url.isEnabled = value
    }

    private fun setupOnClickListener() {
        button_connect.setOnClickListener {
            val url = text_server_url.textContent.ifEmpty(text_server_url.hintContent)
            presenter.connect(text_server_protocol.textContent + url)
        }
    }
}