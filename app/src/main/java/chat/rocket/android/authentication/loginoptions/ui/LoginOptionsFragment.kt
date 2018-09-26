package chat.rocket.android.authentication.loginoptions.ui

import android.app.Activity
import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.loginoptions.presentation.LoginOptionsPresenter
import chat.rocket.android.authentication.loginoptions.presentation.LoginOptionsView
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.clearLightStatusBar
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.rotateBy
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.webview.oauth.ui.INTENT_OAUTH_CREDENTIAL_SECRET
import chat.rocket.android.webview.oauth.ui.INTENT_OAUTH_CREDENTIAL_TOKEN
import chat.rocket.android.webview.oauth.ui.oauthWebViewIntent
import chat.rocket.android.webview.sso.ui.INTENT_SSO_TOKEN
import chat.rocket.android.webview.sso.ui.ssoWebViewIntent
import chat.rocket.common.util.ifNull
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_authentication_login_options.*
import javax.inject.Inject

private const val BUNDLE_SERVER_NAME = "BUNDLE_SERVER_NAME"
private const val DEEP_LINK_INFO = "DeepLinkInfo"

internal const val REQUEST_CODE_FOR_OAUTH = 1
internal const val REQUEST_CODE_FOR_CAS = 2
internal const val REQUEST_CODE_FOR_SAML = 3

fun newInstance(
    serverName: String,
    deepLinkInfo: LoginDeepLinkInfo? = null
): Fragment {
    return LoginOptionsFragment().apply {
        arguments = Bundle(2).apply {
            putString(BUNDLE_SERVER_NAME, serverName)
            putParcelable(DEEP_LINK_INFO, deepLinkInfo)
        }
    }
}

class LoginOptionsFragment : Fragment(), LoginOptionsView {
    @Inject
    lateinit var presenter: LoginOptionsPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private var deepLinkInfo: LoginDeepLinkInfo? = null
    private var serverName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        val bundle = arguments
        if (bundle != null) {
            serverName = bundle.getString(BUNDLE_SERVER_NAME)
            deepLinkInfo = bundle.getParcelable(DEEP_LINK_INFO)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_authentication_login_options)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        presenter.setupView()
        analyticsManager.logScreenView(ScreenViewEvent.LoginOptions)

        deepLinkInfo?.let {
            presenter.authenticateWithDeepLink(it)
        }.ifNull {
            presenter.setupView()
        }
    }

    private fun setupToolbar() {
        with(activity as AuthenticationActivity) {
            this.clearLightStatusBar()
            toolbar.isVisible = true
            toolbar.title = serverName?.replace(getString(R.string.default_protocol), "")
        }
    }

    // OAuth Accounts.
    override fun enableLoginByFacebook() = enableAccountButton(button_facebook)

    override fun setupFacebookButtonListener(facebookOauthUrl: String, state: String) =
        setupButtonListener(button_facebook, facebookOauthUrl, state, REQUEST_CODE_FOR_OAUTH)

    override fun enableLoginByGithub() = enableAccountButton(button_github)

    override fun setupGithubButtonListener(githubUrl: String, state: String) =
        setupButtonListener(button_github, githubUrl, state, REQUEST_CODE_FOR_OAUTH)

    override fun enableLoginByGoogle() = enableAccountButton(button_google)

    override fun setupGoogleButtonListener(googleUrl: String, state: String) =
        setupButtonListener(button_google, googleUrl, state, REQUEST_CODE_FOR_OAUTH)

    override fun enableLoginByLinkedin() = enableAccountButton(button_linkedin)

    override fun setupLinkedinButtonListener(linkedinUrl: String, state: String) =
        setupButtonListener(button_linkedin, linkedinUrl, state, REQUEST_CODE_FOR_OAUTH)

    override fun enableLoginByGitlab() = enableAccountButton(button_gitlab)

    override fun setupGitlabButtonListener(gitlabUrl: String, state: String) =
        setupButtonListener(button_gitlab, gitlabUrl, state, REQUEST_CODE_FOR_OAUTH)

    override fun enableLoginByWordpress() = enableAccountButton(button_wordpress)

    override fun setupWordpressButtonListener(wordpressUrl: String, state: String) =
        setupButtonListener(button_wordpress, wordpressUrl, state, REQUEST_CODE_FOR_OAUTH)

    // CAS service account.
    override fun enableLoginByCas() = enableAccountButton(button_cas)

    override fun setupCasButtonListener(casUrl: String, casToken: String) =
        setupButtonListener(button_cas, casUrl, casToken, REQUEST_CODE_FOR_CAS)

    // Custom OAuth account.
    override fun addCustomOauthButton(
        customOauthUrl: String,
        state: String,
        serviceName: String,
        serviceNameColor: Int,
        buttonColor: Int
    ) {
        val button = getCustomServiceButton(serviceName, serviceNameColor, buttonColor)
        accounts_container.addView(button)
        setupButtonListener(button, customOauthUrl, state, REQUEST_CODE_FOR_OAUTH)
    }

    // SAML account.
    override fun addSamlButton(
        samlUrl: String,
        samlToken: String,
        serviceName: String,
        serviceNameColor: Int,
        buttonColor: Int
    ) {
        val button = getCustomServiceButton(serviceName, serviceNameColor, buttonColor)
        accounts_container.addView(button)
        setupButtonListener(button, samlUrl, samlToken, REQUEST_CODE_FOR_SAML)
    }

    override fun showAccountsView() {
        ui {
            showThreeAccountsMethods()
            accounts_container.isVisible = true
        }
    }

    override fun setupExpandAccountsView() {
        ui {
            expand_more_accounts_container.isVisible = true
            button_expand_collapse_accounts.setOnClickListener { view ->
                if (view.rotation == 0F) {
                    button_expand_collapse_accounts.rotateBy(180F, 400)
                    expandAccountsView()
                } else {
                    button_expand_collapse_accounts.rotateBy(180F, 400)
                    collapseAccountsView()
                }
            }
        }
    }

    override fun showLoginWithEmailButton() {
        ui { _ ->
            button_login_with_email.setOnClickListener { presenter.toLoginWithEmail() }
            button_login_with_email.isVisible = true
        }
    }

    override fun showCreateNewAccountButton() {
        ui { _ ->
            button_create_an_account.setOnClickListener { presenter.toCreateAccount() }
            button_create_an_account.isVisible = true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                REQUEST_CODE_FOR_OAUTH -> {
                    presenter.authenticateWithOauth(
                        data.getStringExtra(INTENT_OAUTH_CREDENTIAL_TOKEN),
                        data.getStringExtra(INTENT_OAUTH_CREDENTIAL_SECRET)
                    )
                }
                REQUEST_CODE_FOR_CAS -> presenter.authenticateWithCas(
                    data.getStringExtra(INTENT_SSO_TOKEN)
                )
                REQUEST_CODE_FOR_SAML -> data.apply {
                    presenter.authenticateWithSaml(getStringExtra(INTENT_SSO_TOKEN))
                }
            }
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

    private fun enableAccountButton(button: Button) {
        ui {
            button.isClickable = true
        }
    }

    private fun setupButtonListener(
        button: Button,
        accountUrl: String,
        argument: String,
        requestCode: Int
    ) {
        ui { activity ->
            button.setOnClickListener {
                when (requestCode) {
                    REQUEST_CODE_FOR_OAUTH -> startActivityForResult(
                        activity.oauthWebViewIntent(accountUrl, argument), REQUEST_CODE_FOR_OAUTH
                    )
                    REQUEST_CODE_FOR_CAS -> startActivityForResult(
                        activity.ssoWebViewIntent(accountUrl, argument), REQUEST_CODE_FOR_CAS
                    )
                    REQUEST_CODE_FOR_SAML -> startActivityForResult(
                        activity.ssoWebViewIntent(accountUrl, argument), REQUEST_CODE_FOR_SAML
                    )
                }

                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
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

        val marginTop = resources.getDimensionPixelSize(R.dimen.button_account_margin_top)
        params.setMargins(0, marginTop, 0, 0)

        val button = Button(context)
        button.layoutParams = params
        button.text = buttonText
        button.setTextColor(buttonTextColor)
        button.background.setColorFilter(buttonBgColor, PorterDuff.Mode.MULTIPLY)

        return button
    }

    private fun showThreeAccountsMethods() {
        (0..accounts_container.childCount)
            .mapNotNull { accounts_container.getChildAt(it) as? Button }
            .filter { it.isClickable }
            .take(3)
            .forEach { it.isVisible = true }
    }

    private fun expandAccountsView() {
        (0..accounts_container.childCount)
            .mapNotNull { accounts_container.getChildAt(it) as? Button }
            .filter { it.isClickable }
            .forEach { it.isVisible = true }
    }

    private fun collapseAccountsView() {
        (0..accounts_container.childCount)
            .mapNotNull { accounts_container.getChildAt(it) as? Button }
            .filter { it.isVisible }
            .drop(3)
            .forEach { it.isVisible = false }
    }

}
