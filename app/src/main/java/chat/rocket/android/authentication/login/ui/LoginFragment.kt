package chat.rocket.android.authentication.login.ui

import DrawableHelper
import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.core.view.isVisible
import androidx.core.view.postDelayed
import chat.rocket.android.R
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.login.presentation.LoginPresenter
import chat.rocket.android.authentication.login.presentation.LoginView
import chat.rocket.android.helper.*
import chat.rocket.android.server.domain.AnalyticsTrackingInteractor
import chat.rocket.android.util.extensions.*
import chat.rocket.android.util.helper.analytics.AnalyticsManager
import chat.rocket.android.util.helper.analytics.event.ScreenViewEvent
import chat.rocket.android.webview.sso.ui.INTENT_SSO_TOKEN
import chat.rocket.android.webview.sso.ui.ssoWebViewIntent
import chat.rocket.android.webview.oauth.ui.INTENT_OAUTH_CREDENTIAL_SECRET
import chat.rocket.android.webview.oauth.ui.INTENT_OAUTH_CREDENTIAL_TOKEN
import chat.rocket.android.webview.oauth.ui.oauthWebViewIntent
import chat.rocket.common.util.ifNull
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_log_in.*
import javax.inject.Inject

internal const val TAG_LOGIN_FRAGMENT = "LoginFragment"
internal const val REQUEST_CODE_FOR_SIGN_IN_REQUIRED = 1
internal const val REQUEST_CODE_FOR_MULTIPLE_ACCOUNTS_RESOLUTION = 2
internal const val REQUEST_CODE_FOR_SAVE_RESOLUTION = 3
internal const val REQUEST_CODE_FOR_CAS = 4
internal const val REQUEST_CODE_FOR_SAML = 5
internal const val REQUEST_CODE_FOR_OAUTH = 6

class LoginFragment : Fragment(), LoginView {
    @Inject
    lateinit var presenter: LoginPresenter
    @Inject
    lateinit var analyticsTrackingInteractor: AnalyticsTrackingInteractor
    private var isOauthViewEnable = false
    private var deepLinkInfo: LoginDeepLinkInfo? = null

    companion object {
        private const val DEEP_LINK_INFO = "DeepLinkInfo"

        fun newInstance(deepLinkInfo: LoginDeepLinkInfo? = null) = LoginFragment().apply {
            arguments = Bundle().apply {
                putParcelable(DEEP_LINK_INFO, deepLinkInfo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
        deepLinkInfo = arguments?.getParcelable(DEEP_LINK_INFO)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =
        container?.inflate(R.layout.fragment_authentication_log_in)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        deepLinkInfo?.let {
            presenter.authenticateWithDeepLink(it)
        }.ifNull {
            presenter.setupView()
        }

        if (!hasCredentialsSupport()) {
            image_key.isVisible = false
        }

        if (analyticsTrackingInteractor.get()) {
            AnalyticsManager.logScreenView(ScreenViewEvent.Login)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                when (requestCode) {
                    REQUEST_CODE_FOR_MULTIPLE_ACCOUNTS_RESOLUTION -> {
                        getCredentials(data)?.let {
                            onCredentialRetrieved(it.first, it.second)
                        }
                    }
                    REQUEST_CODE_FOR_SIGN_IN_REQUIRED -> {
                        getCredentials(data)?.let { credential ->
                            text_username_or_email.setText(credential.first)
                            text_password.setText(credential.second)
                        }
                    }
                    REQUEST_CODE_FOR_SAVE_RESOLUTION -> {
                        showMessage(getString(R.string.message_credentials_saved_successfully))
                    }
                    REQUEST_CODE_FOR_CAS -> {
                        presenter.authenticateWithCas(data.getStringExtra(INTENT_SSO_TOKEN))
                    }
                    REQUEST_CODE_FOR_SAML -> data.apply {
                        presenter.authenticateWithSaml(getStringExtra(INTENT_SSO_TOKEN))
                    }
                    REQUEST_CODE_FOR_OAUTH -> {
                        presenter.authenticateWithOauth(
                            data.getStringExtra(INTENT_OAUTH_CREDENTIAL_TOKEN),
                            data.getStringExtra(INTENT_OAUTH_CREDENTIAL_SECRET)
                        )
                    }
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

    private fun tintEditTextDrawableStart() {
        ui {
            val personDrawable =
                DrawableHelper.getDrawableFromId(R.drawable.ic_assignment_ind_black_24dp, it)
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, it)

            val drawables = arrayOf(personDrawable, lockDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, it, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(
                arrayOf(text_username_or_email, text_password),
                drawables
            )
        }
    }

    private fun requestStoredCredentials() {
        activity?.let {
            it.requestStoredCredentials()?.let { credentials ->
                onCredentialRetrieved(credentials.first, credentials.second)
            }
        }
    }

    private fun onCredentialRetrieved(id: String, password: String) {
        presenter.authenticateWithUserAndPassword(id, password)
    }

    override fun saveSmartLockCredentials(id: String, password: String) {
        activity?.let {
            it.saveCredentials(id, password)
        }
    }

    override fun showLoading() {
        ui {
            view_loading.isVisible = true
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.isVisible = false
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
        showMessage(R.string.msg_generic_error)
    }

    override fun showFormView() {
        ui {
            text_username_or_email.isVisible = true
            text_password.isVisible = true
            image_key.isVisible = true
        }
    }

    override fun hideFormView() {
        ui {
            text_username_or_email.isVisible = false
            text_password.isVisible = false
        }
    }

    override fun setupLoginButtonListener() {
        ui {
            button_log_in.setOnClickListener {
                presenter.authenticateWithUserAndPassword(
                    text_username_or_email.textContent,
                    text_password.textContent
                )
            }
        }
    }

    override fun enableUserInput() {
        ui {
            button_log_in.isEnabled = true
            text_username_or_email.isEnabled = true
            text_password.isEnabled = true
        }
    }

    override fun disableUserInput() {
        ui {
            button_log_in.isEnabled = false
            text_username_or_email.isEnabled = false
            text_password.isEnabled = false
        }
    }

    override fun showCasButton() {
        ui {
            button_cas.isVisible = true
        }
    }

    override fun hideCasButton() {
        ui {
            button_cas.isVisible = false
        }
    }

    override fun setupCasButtonListener(casUrl: String, casToken: String) {
        ui { activity ->
            button_cas.setOnClickListener {
                startActivityForResult(
                    activity.ssoWebViewIntent(casUrl, casToken),
                    REQUEST_CODE_FOR_CAS
                )
                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
        }
    }

    override fun showForgotPasswordView() {
        ui {
            text_forgot_your_password.isVisible = true
        }
    }

    override fun setupForgotPasswordView() {
        ui {
            val reset = getString(R.string.msg_reset)
            val forgotPassword = String.format(getString(R.string.msg_forgot_password), reset)

            text_forgot_your_password.text = forgotPassword

            val resetListener = object : ClickableSpan() {
                override fun onClick(view: View) = presenter.forgotPassword()
            }

            TextHelper.addLink(text_forgot_your_password, arrayOf(reset), arrayOf(resetListener))
        }
    }

    override fun addCustomOauthServiceButton(
        customOauthUrl: String,
        state: String,
        serviceName: String,
        serviceNameColor: Int,
        buttonColor: Int
    ) {
        ui { activity ->
            val button = getCustomServiceButton(serviceName, serviceNameColor, buttonColor)

            button.setOnClickListener {
                startActivityForResult(
                    activity.oauthWebViewIntent(customOauthUrl, state),
                    REQUEST_CODE_FOR_OAUTH
                )
                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
        }
    }

    override fun addSamlServiceButton(
        samlUrl: String,
        samlToken: String,
        serviceName: String,
        serviceNameColor: Int,
        buttonColor: Int
    ) {
        ui { activity ->
            val button = getCustomServiceButton(serviceName, serviceNameColor, buttonColor)

            button.setOnClickListener {
                startActivityForResult(
                    activity.ssoWebViewIntent(samlUrl, samlToken),
                    REQUEST_CODE_FOR_SAML
                )
                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
        }
    }

    override fun alertWrongUsernameOrEmail() {
        ui {
            vibrateSmartPhone()
            text_username_or_email.shake()
            text_username_or_email.requestFocus()
        }
    }

    override fun alertWrongPassword() {
        ui {
            vibrateSmartPhone()
            text_password.shake()
            text_password.requestFocus()
        }
    }

    /**
     * Gets a stylized custom service button.
     */
    private fun getCustomServiceButton(
        buttonText: String,
        buttonTextColor: Int,
        buttonBgColor: Int
    ): Button {
        val params: LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val margin = resources.getDimensionPixelSize(R.dimen.screen_edge_left_and_right_margins)
        params.setMargins(margin, margin, margin, 0)

        val button = Button(context)
        button.layoutParams = params
        button.text = buttonText
        button.setTextColor(buttonTextColor)
        button.background.setColorFilter(buttonBgColor, PorterDuff.Mode.MULTIPLY)

        return button
    }
}