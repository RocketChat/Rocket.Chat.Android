package chat.rocket.android.authentication.twofactor.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.twofactor.presentation.TwoFAPresenter
import chat.rocket.android.authentication.twofactor.presentation.TwoFAView
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_authentication_two_fa.*
import javax.inject.Inject

fun newInstance(username: String, password: String): Fragment = TwoFAFragment().apply {
    arguments = Bundle(2).apply {
        putString(BUNDLE_USERNAME, username)
        putString(BUNDLE_PASSWORD, password)
    }
}

private const val BUNDLE_USERNAME = "username"
private const val BUNDLE_PASSWORD = "password"

class TwoFAFragment : Fragment(), TwoFAView {
    @Inject
    lateinit var presenter: TwoFAPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private lateinit var username: String
    private lateinit var password: String
    private lateinit var twoFaCodeDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            username = getString(BUNDLE_USERNAME, "")
            password = getString(BUNDLE_PASSWORD, "")
        } ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_authentication_two_fa)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.apply {
            text_two_factor_authentication_code.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(
                text_two_factor_authentication_code,
                InputMethodManager.RESULT_UNCHANGED_SHOWN
            )
        }

        setupOnClickListener()
        subscribeEditText()

        analyticsManager.logScreenView(ScreenViewEvent.TwoFa)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeEditText()
    }

    override fun enableButtonConfirm() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_confirm, ContextCompat.getColorStateList(it, R.color.colorAccent)
            )
            button_confirm.isEnabled = true
        }
    }

    override fun disableButtonConfirm() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_confirm,
                ContextCompat.getColorStateList(it, R.color.colorAuthenticationButtonDisabled)
            )
            button_confirm.isEnabled = false
        }
    }

    override fun alertInvalidTwoFactorAuthenticationCode() =
        showMessage(R.string.msg_invalid_2fa_code)

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

    override fun showGenericErrorMessage() = showMessage(R.string.msg_generic_error)

    private fun enableUserInput() {
        enableButtonConfirm()
        text_two_factor_authentication_code.isEnabled = true
    }

    private fun disableUserInput() {
        disableButtonConfirm()
        text_two_factor_authentication_code.isEnabled = false
    }

    private fun setupOnClickListener() {
        button_confirm.setOnClickListener {
            presenter.authenticate(
                username,
                password,
                text_two_factor_authentication_code.textContent
            )
        }
    }

    private fun subscribeEditText() {
        twoFaCodeDisposable = text_two_factor_authentication_code.asObservable()
            .subscribe {
                if (it.isNotBlank()) {
                    enableButtonConfirm()
                } else {
                    disableButtonConfirm()
                }
            }
    }

    private fun unsubscribeEditText() = twoFaCodeDisposable.dispose()
}
