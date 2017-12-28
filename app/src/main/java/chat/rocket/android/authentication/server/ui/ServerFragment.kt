package chat.rocket.android.authentication.server.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.authentication.server.presentation.ServerPresenter
import chat.rocket.android.authentication.server.presentation.ServerView
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.util.hintContent
import chat.rocket.android.util.ifEmpty
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_server.*
import javax.inject.Inject

class ServerFragment : Fragment(), ServerView {
    @Inject lateinit var presenter: ServerPresenter
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        text_server_url.isCursorVisible = KeyboardHelper.isSoftKeyboardShown(relative_layout.rootView)
    }

    companion object {
        fun newInstance() = ServerFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_authentication_server, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        relative_layout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

        activity?.applicationContext?.apply {
            button_connect.setOnClickListener {
                val url = text_server_url.textContent.ifEmpty(text_server_url.hintContent)
                presenter.connect(text_server_protocol.textContent + url)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        relative_layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
    }

    override fun showLoading() {
        text_server_url.isEnabled = false
        button_connect.isEnabled = false
        view_loading.setVisibility(true)
    }

    override fun hideLoading() {
        view_loading.setVisibility(false)
        button_connect.isEnabled = true
        text_server_url.isEnabled = true
    }

    override fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showNoInternetConnection() {
        Toast.makeText(activity, getString(R.string.msg_no_internet_connection), Toast.LENGTH_SHORT).show()
    }
}