package chat.rocket.android.authentication.twofactor.ui

import DrawableHelper
import android.content.Context
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
import chat.rocket.android.helper.AnimationHelper
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_two_fa.*
import javax.inject.Inject

class TwoFAFragment : Fragment(), TwoFAView {
    @Inject lateinit var presenter: TwoFAPresenter
    @Inject lateinit var appContext: Context    
    lateinit var serverUrl: String
    lateinit var username: String
    lateinit var password: String

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

    override fun alertBlankTwoFactorAuthenticationCode() {
        AnimationHelper.vibrateSmartPhone(appContext)
        AnimationHelper.shakeView(text_two_factor_auth)
    }

    override fun alertInvalidTwoFactorAuthenticationCode() {
        showMessage(getString(R.string.msg_invalid_2fa_code))
    }

    override fun showLoading() {
        view_loading.setVisibility(true)
        enableUserInput(false)
        view_loading.show()
    }

    override fun hideLoading() {
        view_loading.hide()
        enableUserInput(true)
    }

    override fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun showNoInternetConnection() {
        showMessage(getString(R.string.msg_no_internet_connection))
    }

    private fun tintEditTextDrawableStart() {
        activity?.applicationContext?.apply {
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_vpn_key_black_24dp, this)

            DrawableHelper.wrapDrawable(lockDrawable)
            DrawableHelper.tintDrawable(lockDrawable, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawable(text_two_factor_auth, lockDrawable)
        }
    }

    private fun enableUserInput(value: Boolean) {
        button_log_in.isEnabled = value
        text_two_factor_auth.isEnabled = value
    }
}