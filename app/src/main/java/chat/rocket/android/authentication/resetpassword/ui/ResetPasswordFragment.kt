package chat.rocket.android.authentication.resetpassword.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.resetpassword.presentation.ResetPasswordPresenter
import chat.rocket.android.authentication.resetpassword.presentation.ResetPasswordView
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.isEmail
import chat.rocket.android.util.extensions.showKeyboard
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_authentication_reset_password.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

fun newInstance(): Fragment = ResetPasswordFragment()

class ResetPasswordFragment : Fragment(), ResetPasswordView {
    @Inject
    lateinit var presenter: ResetPasswordPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private lateinit var emailAddressDisposable: Disposable

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

        setupOnClickListener()
        subscribeEditText()

        analyticsManager.logScreenView(ScreenViewEvent.ResetPassword)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeEditText()
    }

    override fun emailSent() = showMessage(R.string.msg_check_your_email_to_reset_your_password)

    override fun updateYourServerVersion() =
        showMessage(R.string.msg_update_app_version_in_order_to_continue)

    override fun enableButtonConnect() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_reset_password, ContextCompat.getColorStateList(it, R.color.colorAccent)
            )
            button_reset_password.isEnabled = true
        }
    }

    override fun disableButtonConnect() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_reset_password,
                ContextCompat.getColorStateList(it, R.color.colorAuthenticationButtonDisabled)
            )
            button_reset_password.isEnabled = false
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

    private fun enableUserInput() {
        enableButtonConnect()
        text_email.isEnabled = true
    }

    private fun disableUserInput() {
        disableButtonConnect()
        text_email.isEnabled = false
    }

    private fun setupOnClickListener() =
        button_reset_password.setOnClickListener {
            presenter.resetPassword(text_email.textContent)
        }

    private fun subscribeEditText() {
        emailAddressDisposable = text_email.asObservable()
            .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .filter { it.isNotBlank() }
            .subscribe {
                if (it.toString().isEmail()) {
                    enableButtonConnect()
                } else {
                    disableButtonConnect()
                }
            }
    }

    private fun unsubscribeEditText() = emailAddressDisposable.dispose()
}