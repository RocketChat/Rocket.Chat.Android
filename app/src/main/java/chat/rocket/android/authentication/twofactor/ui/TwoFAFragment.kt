package chat.rocket.android.authentication.twofactor.ui

import DrawableHelper
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.authentication.twofactor.presentation.TwoFAPresenter
import chat.rocket.android.authentication.twofactor.presentation.TwoFAView
import chat.rocket.android.util.textContent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_two_fa.*
import javax.inject.Inject

class TwoFAFragment : Fragment(), TwoFAView {

    companion object {
        private const val SERVER_URL = "server_url"
        private const val USERNAME = "username"
        private const val PASSWORD = "password"

        fun newInstance(url: String, username: String, password: String) = TwoFAFragment().apply {
            arguments = Bundle(1).apply {
                putString(SERVER_URL, url)
                putString(USERNAME, username)
                putString(PASSWORD, password)
            }
        }
    }

    var progress: ProgressDialog? = null
    lateinit var serverUrl: String
    lateinit var username: String
    lateinit var password: String

    @Inject
    lateinit var presenter: TwoFAPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        // TODO - research a better way to initialize parameters on fragments.
        serverUrl = arguments?.getString(SERVER_URL) ?: "https://open.rocket.chat"
        username = arguments?.getString(USERNAME) ?: ""
        password = arguments?.getString(PASSWORD) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_authentication_two_fa, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        button_log_in.setOnClickListener {
            presenter.authenticate(username, password, text_two_factor_auth.textContent)
        }
    }

    private fun tintEditTextDrawableStart() {
        activity?.applicationContext?.apply {
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_vpn_key_black_24dp, this)

            DrawableHelper.wrapDrawable(lockDrawable)
            DrawableHelper.tintDrawable(lockDrawable, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawable(text_two_factor_auth, lockDrawable)
        }
    }

    override fun showLoading() {
        // TODO - change for a proper progress indicator
        progress = ProgressDialog.show(activity, "Authenticating",
                "Verifying user credentials", true, true)
    }

    override fun hideLoading() {
        progress?.apply {
            cancel()
        }
        progress = null
    }

    override fun onLoginError(message: String?) {
        // TODO - show a proper error message
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
}