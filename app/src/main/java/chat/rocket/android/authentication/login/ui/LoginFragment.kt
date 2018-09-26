package chat.rocket.android.authentication.login.ui

import android.app.Activity
import android.content.Intent
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
import chat.rocket.android.authentication.login.presentation.LoginPresenter
import chat.rocket.android.authentication.login.presentation.LoginView
import chat.rocket.android.helper.getCredentials
import chat.rocket.android.helper.hasCredentialsSupport
import chat.rocket.android.helper.requestStoredCredentials
import chat.rocket.android.helper.saveCredentials
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.fragment_authentication_log_in.*
import javax.inject.Inject

internal const val REQUEST_CODE_FOR_SIGN_IN_REQUIRED = 1
internal const val REQUEST_CODE_FOR_MULTIPLE_ACCOUNTS_RESOLUTION = 2
internal const val REQUEST_CODE_FOR_SAVE_RESOLUTION = 3

fun newInstance() = LoginFragment()

class LoginFragment : Fragment(), LoginView {
    @Inject
    lateinit var presenter: LoginPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private val editTextsDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_authentication_log_in)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setupView()

        subscribeEditTexts()
        setupOnClickListener()

        image_key.isVisible = hasCredentialsSupport()
        analyticsManager.logScreenView(ScreenViewEvent.Login)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                when (requestCode) {
                    REQUEST_CODE_FOR_MULTIPLE_ACCOUNTS_RESOLUTION ->
                        getCredentials(data)?.let {
                            onCredentialRetrieved(it.first, it.second)
                        }
                    REQUEST_CODE_FOR_SIGN_IN_REQUIRED ->
                        getCredentials(data)?.let { credential ->
                            text_username_or_email.setText(credential.first)
                            text_password.setText(credential.second)
                        }
                    REQUEST_CODE_FOR_SAVE_RESOLUTION -> showMessage(getString(R.string.message_credentials_saved_successfully))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        image_key.setOnClickListener {
            requestStoredCredentials()
            image_key.isVisible = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeEditTexts()
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

    override fun showGenericErrorMessage() = showMessage(R.string.msg_generic_error)

    private fun setupOnClickListener() =
        ui { _ ->
            button_log_in.setOnClickListener {
                presenter.authenticateWithUserAndPassword(
                    text_username_or_email.textContent,
                    text_password.textContent
                )
            }
        }

    override fun showForgotPasswordView() {
        ui { _ ->
            button_forgot_your_password.isVisible = true
            button_forgot_your_password.setOnClickListener { presenter.forgotPassword() }

        }
    }

    override fun enableButtonLogin() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_log_in, ContextCompat.getColorStateList(it, R.color.colorAccent)
            )
            button_log_in.isEnabled = true
        }

    }

    override fun disableButtonLogin() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_log_in,
                ContextCompat.getColorStateList(it, R.color.colorAuthenticationButtonDisabled)
            )
            button_log_in.isEnabled = false
        }
    }

    private fun requestStoredCredentials() {
        activity?.requestStoredCredentials()?.let { credentials ->
            onCredentialRetrieved(credentials.first, credentials.second)
        }
    }

    private fun onCredentialRetrieved(id: String, password: String) {
        presenter.authenticateWithUserAndPassword(id, password)
    }

    override fun saveSmartLockCredentials(id: String, password: String) {
        activity?.saveCredentials(id, password)
    }

    private fun subscribeEditTexts() {
        editTextsDisposable.add(
            Observables.combineLatest(
                text_username_or_email.asObservable(),
                text_password.asObservable()
            ) { text_username_or_email, text_password ->
                return@combineLatest (
                        text_username_or_email.isNotBlank() && text_password.isNotBlank()
                        )
            }.subscribe { isValid ->
                if (isValid) {
                    enableButtonLogin()
                } else {
                    disableButtonLogin()
                }
            })
    }

    private fun unsubscribeEditTexts() = editTextsDisposable.clear()

    private fun enableUserInput() {
        ui {
            enableButtonLogin()
            text_username_or_email.isEnabled = true
            text_password.isEnabled = true
        }
    }

    private fun disableUserInput() {
        ui {
            disableButtonLogin()
            text_username_or_email.isEnabled = false
            text_password.isEnabled = false
        }
    }
}
