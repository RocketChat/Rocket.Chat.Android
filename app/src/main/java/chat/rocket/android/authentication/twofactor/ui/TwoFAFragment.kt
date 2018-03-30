package chat.rocket.android.authentication.twofactor.ui

import DrawableHelper
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import chat.rocket.android.R
import chat.rocket.android.authentication.twofactor.presentation.TwoFAPresenter
import chat.rocket.android.authentication.twofactor.presentation.TwoFAView
import chat.rocket.android.util.extensions.*
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_two_fa.*
import javax.inject.Inject

class TwoFAFragment : Fragment(), TwoFAView {
    @Inject lateinit var presenter: TwoFAPresenter
    lateinit var username: String
    lateinit var password: String

    // TODO - we could create an in memory repository to save username and password.
    companion object {
        private const val USERNAME = "username"
        private const val PASSWORD = "password"

        fun newInstance(username: String, password: String) = TwoFAFragment().apply {
            arguments = Bundle(1).apply {
                putString(USERNAME, username)
                putString(PASSWORD, password)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        // TODO - research a better way to initialize parameters on fragments.
        username = arguments?.getString(USERNAME) ?: ""
        password = arguments?.getString(PASSWORD) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_authentication_two_fa, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.apply {
            text_two_factor_auth.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(text_two_factor_auth, InputMethodManager.RESULT_UNCHANGED_SHOWN)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }
        setupOnClickListener()
    }

    override fun alertBlankTwoFactorAuthenticationCode() {
        vibrateSmartPhone()
        text_two_factor_auth.shake()
    }

    override fun alertInvalidTwoFactorAuthenticationCode() {
        showMessage(getString(R.string.msg_invalid_2fa_code))
    }

    override fun showLoading() {
        enableUserInput(false)
        view_loading.setVisible(true)
    }

    override fun hideLoading() {
        view_loading.setVisible(false)
        enableUserInput(true)
    }

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showNoInternetConnection() = showMessage(getString(R.string.msg_no_internet_connection))

    private fun tintEditTextDrawableStart() {
        activity?.apply {
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

    private fun setupOnClickListener() {
        button_log_in.setOnClickListener {
            presenter.authenticate(username, password, text_two_factor_auth.textContent)
        }
    }
}
