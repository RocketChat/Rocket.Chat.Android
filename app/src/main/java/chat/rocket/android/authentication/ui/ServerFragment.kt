package chat.rocket.android.authentication.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import chat.rocket.android.R
import chat.rocket.android.authentication.presentation.ServerPresenter
import chat.rocket.android.authentication.presentation.ServerView
import chat.rocket.android.util.ifEmpty
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_server.*
import javax.inject.Inject

class ServerFragment : Fragment(), ServerView {

    @Inject
    lateinit var presenter: ServerPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_authentication_server, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        text_server_url.setSelection(text_server_url.length())

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        button_connect.setOnClickListener {
            val url = text_server_url.text.toString().ifEmpty(text_server_url.hint.toString())
            presenter.login(server_protocol_label.text.toString() + url)
        }
    }

    companion object {
        fun newInstance() = ServerFragment()
    }
}