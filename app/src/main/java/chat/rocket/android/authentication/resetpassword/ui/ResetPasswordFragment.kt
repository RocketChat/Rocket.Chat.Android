package chat.rocket.android.authentication.resetpassword.ui

import DrawableHelper
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.resetpassword.presentation.ResetPasswordPresenter
import chat.rocket.android.authentication.resetpassword.presentation.ResetPasswordView
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.shake
import chat.rocket.android.util.extensions.showKeyboard
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.util.extensions.vibrateSmartPhone
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_reset_password.*
import javax.inject.Inject

internal const val TAG_RESET_PASSWORD_FRAGMENT = "ResetPasswordFragment"

class ResetPasswordFragment : Fragment(), ResetPasswordView {
    @Inject
    lateinit var presenter: ResetPasswordPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_authentication_reset_password)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.apply {
            text_email.requestFocus()
            showKeyboard(text_email)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        setupOnClickListener()

        analyticsManager.logScreenView(ScreenViewEvent.ResetPassword)
    }

    override fun alertBlankEmail() {
        ui {
            vibrateShakeAndRequestFocusForTextEmail()
        }
    }

    override fun alertInvalidEmail() {
        ui {
            vibrateShakeAndRequestFocusForTextEmail()
            showMessage(R.string.msg_invalid_email)
        }
    }

    override fun emailSent() {
        showToast(R.string.msg_check_your_email_to_reset_your_password, Toast.LENGTH_LONG)
    }

    override fun updateYourServerVersion() {
        showMessage(R.string.msg_update_app_version_in_order_to_continue)
    }

    override fun showLoading() {
        ui {
            disableUserInput()
            view_loading.setVisible(true)
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.setVisible(false)
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

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    private fun tintEditTextDrawableStart() {
        ui {
            val emailDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_email_black_24dp, it)
            DrawableHelper.wrapDrawable(emailDrawable)
            DrawableHelper.tintDrawable(emailDrawable, it, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawable(text_email, emailDrawable)
        }
    }

    private fun enableUserInput() {
        button_reset_password.isEnabled = true
        text_email.isEnabled = true
    }

    private fun disableUserInput() {
        button_reset_password.isEnabled = false
        text_email.isEnabled = true
    }

    private fun vibrateShakeAndRequestFocusForTextEmail() {
        vibrateSmartPhone()
        text_email.shake()
        text_email.requestFocus()
    }

    private fun setupOnClickListener() {
        button_reset_password.setOnClickListener {
            presenter.resetPassword(text_email.textContent)
        }
    }

    companion object {
        fun newInstance() = ResetPasswordFragment()
    }
}