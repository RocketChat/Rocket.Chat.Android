package chat.rocket.android.authentication.signup.ui

import DrawableHelper
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.authentication.login.ui.googleApiClient
import chat.rocket.android.authentication.signup.presentation.SignupPresenter
import chat.rocket.android.authentication.signup.presentation.SignupView
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.helper.TextHelper
import chat.rocket.android.util.extensions.*
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.common.api.ResolvingResultCallbacks
import com.google.android.gms.common.api.Status
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_sign_up.*
import timber.log.Timber
import javax.inject.Inject

internal const val SAVE_CREDENTIALS = 1

class SignupFragment : Fragment(), SignupView {

    @Inject
    lateinit var presenter: SignupPresenter
    private lateinit var credentialsToBeSaved: Credential
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (KeyboardHelper.isSoftKeyboardShown(relative_layout.rootView)) {
            bottom_container.setVisible(false)
        } else {
            bottom_container.apply {
                postDelayed({
                    ui { setVisible(true) }
                }, 3)
            }
        }
    }

    companion object {
        fun newInstance() = SignupFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_authentication_sign_up, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        relative_layout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)

        setUpNewUserAgreementListener()

        button_sign_up.setOnClickListener {
            presenter.signup(
                text_username.textContent,
                text_username.textContent,
                text_password.textContent,
                text_email.textContent
            )
        }
    }

    override fun onDestroyView() {
        relative_layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        super.onDestroyView()
    }

    override fun alertBlankName() {
        ui {
            vibrateSmartPhone()
            text_name.shake()
            text_name.requestFocus()
        }
    }

    override fun alertBlankUsername() {
        ui {
            vibrateSmartPhone()
            text_username.shake()
            text_username.requestFocus()
        }
    }

    override fun alertEmptyPassword() {
        ui {
            vibrateSmartPhone()
            text_password.shake()
            text_password.requestFocus()
        }
    }

    override fun alertBlankEmail() {
        ui {
            vibrateSmartPhone()
            text_email.shake()
            text_email.requestFocus()
        }
    }

    override fun saveSmartLockCredentials(loginCredential: Credential) {
        credentialsToBeSaved = loginCredential
        googleApiClient.let {
            if (it.isConnected) {
                saveCredentials()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == SAVE_CREDENTIALS) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(
                    context,
                    getString(R.string.message_credentials_saved_successfully),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Timber.e("ERROR: Cancelled by user")
            }
        }
    }

    private fun saveCredentials() {
        activity?.let {
            Auth.CredentialsApi.save(googleApiClient, credentialsToBeSaved).setResultCallback(
                object : ResolvingResultCallbacks<Status>(it, SAVE_CREDENTIALS) {
                    override fun onSuccess(status: Status) {
                        Timber.d("save:SUCCESS:$status")
                    }

                    override fun onUnresolvableFailure(status: Status) {
                        Timber.e("save:FAILURE:$status")
                    }
                })
        }
    }

    override fun showLoading() {
        ui {
            enableUserInput(false)
            view_loading.setVisible(true)
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.setVisible(false)
            enableUserInput(true)
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
            val personDrawable =
                DrawableHelper.getDrawableFromId(R.drawable.ic_person_black_24dp, it)
            val atDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_at_black_24dp, it)
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, it)
            val emailDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_email_black_24dp, it)

            val drawables = arrayOf(personDrawable, atDrawable, lockDrawable, emailDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, it, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(
                arrayOf(
                    text_name,
                    text_username,
                    text_password,
                    text_email
                ), drawables
            )
        }
    }

    private fun setUpNewUserAgreementListener() {
        val termsOfService = getString(R.string.action_terms_of_service)
        val privacyPolicy = getString(R.string.action_privacy_policy)
        val newUserAgreement =
            String.format(getString(R.string.msg_new_user_agreement), termsOfService, privacyPolicy)

        text_new_user_agreement.text = newUserAgreement

        val termsOfServiceListener = object : ClickableSpan() {
            override fun onClick(view: View) {
                presenter.termsOfService()
            }
        }

        val privacyPolicyListener = object : ClickableSpan() {
            override fun onClick(view: View) {
                presenter.privacyPolicy()
            }
        }

        TextHelper.addLink(
            text_new_user_agreement,
            arrayOf(termsOfService, privacyPolicy),
            arrayOf(termsOfServiceListener, privacyPolicyListener)
        )
    }

    private fun enableUserInput(value: Boolean) {
        button_sign_up.isEnabled = value
        text_username.isEnabled = value
        text_username.isEnabled = value
        text_password.isEnabled = value
        text_email.isEnabled = value
    }
}
