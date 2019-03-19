package chat.rocket.android.authentication.signup.ui

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
import chat.rocket.android.authentication.signup.presentation.SignupPresenter
import chat.rocket.android.authentication.signup.presentation.SignupView
import chat.rocket.android.helper.saveCredentials
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.isEmail
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.fragment_authentication_sign_up.*
import javax.inject.Inject

fun newInstance() = SignupFragment()

internal const val SAVE_CREDENTIALS = 1

class SignupFragment : Fragment(), SignupView {
    @Inject
    lateinit var presenter: SignupPresenter
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
    ): View? = container?.inflate(R.layout.fragment_authentication_sign_up)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeEditTexts()
        setupOnClickListener()

        analyticsManager.logScreenView(ScreenViewEvent.SignUp)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                if (requestCode == SAVE_CREDENTIALS) {
                    showMessage(getString(R.string.msg_credentials_saved_successfully))
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeEditTexts()
    }

    private fun setupOnClickListener() =
        ui {
            button_register.setOnClickListener {
                presenter.signup(
                    text_username.textContent,
                    text_username.textContent,
                    text_password.textContent,
                    text_email.textContent
                )
            }
        }

    override fun enableButtonRegister() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_register, ContextCompat.getColorStateList(it, R.color.colorAccent)
            )
            button_register.isEnabled = true
        }

    }

    override fun disableButtonRegister() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_register,
                ContextCompat.getColorStateList(it, R.color.colorAuthenticationButtonDisabled)
            )
            button_register.isEnabled = false
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

    override fun saveSmartLockCredentials(id: String, password: String) {
        activity?.saveCredentials(id, password)
    }

    private fun subscribeEditTexts() {
        editTextsDisposable.add(
            Observables.combineLatest(
                text_name.asObservable(),
                text_username.asObservable(),
                text_password.asObservable(),
                text_email.asObservable()
            ) { text_name, text_username, text_password, text_email ->
                return@combineLatest (
                        text_name.isNotBlank() &&
                                text_username.isNotBlank() &&
                                text_password.isNotBlank() &&
                                text_email.isNotBlank() &&
                                text_email.toString().isEmail()
                        )
            }.subscribe { isValid ->
                if (isValid) {
                    enableButtonRegister()
                } else {
                    disableButtonRegister()
                }
            })
    }

    private fun unsubscribeEditTexts() = editTextsDisposable.clear()

    private fun enableUserInput() {
        text_name.isEnabled = true
        text_username.isEnabled = true
        text_password.isEnabled = true
        text_email.isEnabled = true
        enableButtonRegister()
    }

    private fun disableUserInput() {
        disableButtonRegister()
        text_name.isEnabled = false
        text_username.isEnabled = false
        text_password.isEnabled = false
        text_email.isEnabled = false
    }
}
