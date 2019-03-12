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
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.domain.model.LoginDeepLinkInfo
import chat.rocket.android.authentication.loginoptions.presentation.LoginOptionsPresenter
import chat.rocket.android.authentication.loginoptions.presentation.LoginOptionsView
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.*
import chat.rocket.android.webview.oauth.ui.INTENT_OAUTH_CREDENTIAL_SECRET
import chat.rocket.android.webview.oauth.ui.INTENT_OAUTH_CREDENTIAL_TOKEN
import chat.rocket.android.webview.oauth.ui.oauthWebViewIntent
import chat.rocket.android.webview.sso.ui.INTENT_SSO_TOKEN
import chat.rocket.android.webview.sso.ui.ssoWebViewIntent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.fragment_authentication_login_options.*
import javax.inject.Inject

private const val SERVER_NAME = "server_name"
private const val STATE = "state"
private const val FACEBOOK_OAUTH_URL = "facebook_oauth_url"
private const val GITHUB_OAUTH_URL = "github_oauth_url"
private const val GOOGLE_OAUTH_URL = "google_oauth_url"
private const val LINKEDIN_OAUTH_URL = "linkedin_oauth_url"
private const val GITLAB_OAUTH_URL = "gitlab_oauth_url"
private const val WORDPRESS_OAUTH_URL = "wordpress_oauth_url"
private const val CAS_LOGIN_URL = "cas_login_url"
private const val CAS_TOKEN = "cas_token"
private const val CAS_SERVICE_NAME = "cas_service_name"
private const val CAS_SERVICE_NAME_TEXT_COLOR = "cas_service_name_text_color"
private const val CAS_SERVICE_BUTTON_COLOR = "cas_service_button_color"
private const val CUSTOM_OAUTH_URL = "custom_oauth_url"
private const val CUSTOM_OAUTH_SERVICE_NAME = "custom_oauth_service_name"
private const val CUSTOM_OAUTH_SERVICE_NAME_TEXT_COLOR = "custom_oauth_service_name_text_color"
private const val CUSTOM_OAUTH_SERVICE_BUTTON_COLOR = "custom_oauth_service_button_color"
private const val SAML_URL = "saml_url"
private const val SAML_TOKEN = "saml_token"
private const val SAML_SERVICE_NAME = "saml_service_name"
private const val SAML_SERVICE_NAME_TEXT_COLOR = "saml_service_name_text_color"
private const val SAML_SERVICE_BUTTON_COLOR = "saml_service_button_color"
private const val TOTAL_SOCIAL_ACCOUNTS = "total_social_accounts"
private const val IS_LOGIN_FORM_ENABLED = "is_login_form_enabled"
private const val IS_NEW_ACCOUNT_CREATION_ENABLED = "is_new_account_creation_enabled"
private const val DEEP_LINK_INFO = "deep-link-info"

internal const val REQUEST_CODE_FOR_OAUTH = 1
internal const val REQUEST_CODE_FOR_CAS = 2
internal const val REQUEST_CODE_FOR_SAML = 3

fun newInstance(
    serverName: String,
    state: String? = null,
    facebookOauthUrl: String? = null,
    githubOauthUrl: String? = null,
    googleOauthUrl: String? = null,
    linkedinOauthUrl: String? = null,
    gitlabOauthUrl: String? = null,
    wordpressOauthUrl: String? = null,
    casLoginUrl: String? = null,
    casToken: String? = null,
    casServiceName: String? = null,
    casServiceNameTextColor: Int = 0,
    casServiceButtonColor: Int = 0,
    customOauthUrl: String? = null,
    customOauthServiceName: String? = null,
    customOauthServiceNameTextColor: Int = 0,
    customOauthServiceButtonColor: Int = 0,
    samlUrl: String? = null,
    samlToken: String? = null,
    samlServiceName: String? = null,
    samlServiceNameTextColor: Int = 0,
    samlServiceButtonColor: Int = 0,
    totalSocialAccountsEnabled: Int = 0,
    isLoginFormEnabled: Boolean,
    isNewAccountCreationEnabled: Boolean,
    deepLinkInfo: LoginDeepLinkInfo? = null
): Fragment = LoginOptionsFragment().apply {
    arguments = Bundle(23).apply {
        putString(SERVER_NAME, serverName)
        putString(STATE, state)
        putString(FACEBOOK_OAUTH_URL, facebookOauthUrl)
        putString(GITHUB_OAUTH_URL, githubOauthUrl)
        putString(GOOGLE_OAUTH_URL, googleOauthUrl)
        putString(LINKEDIN_OAUTH_URL, linkedinOauthUrl)
        putString(GITLAB_OAUTH_URL, gitlabOauthUrl)
        putString(WORDPRESS_OAUTH_URL, wordpressOauthUrl)
        putString(CAS_LOGIN_URL, casLoginUrl)
        putString(CAS_TOKEN, casToken)
        putString(CAS_SERVICE_NAME, casServiceName)
        putInt(CAS_SERVICE_NAME_TEXT_COLOR, casServiceNameTextColor)
        putInt(CAS_SERVICE_BUTTON_COLOR, casServiceButtonColor)
        putString(CUSTOM_OAUTH_URL, customOauthUrl)
        putString(CUSTOM_OAUTH_SERVICE_NAME, customOauthServiceName)
        putInt(CUSTOM_OAUTH_SERVICE_NAME_TEXT_COLOR, customOauthServiceNameTextColor)
        putInt(CUSTOM_OAUTH_SERVICE_BUTTON_COLOR, customOauthServiceButtonColor)
        putString(SAML_URL, samlUrl)
        putString(SAML_TOKEN, samlToken)
        putString(SAML_SERVICE_NAME, samlServiceName)
        putInt(SAML_SERVICE_NAME_TEXT_COLOR, samlServiceNameTextColor)
        putInt(SAML_SERVICE_BUTTON_COLOR, samlServiceButtonColor)
        putInt(TOTAL_SOCIAL_ACCOUNTS, totalSocialAccountsEnabled)
        putBoolean(IS_LOGIN_FORM_ENABLED, isLoginFormEnabled)
        putBoolean(IS_NEW_ACCOUNT_CREATION_ENABLED, isNewAccountCreationEnabled)
        putParcelable(DEEP_LINK_INFO, deepLinkInfo)
    }
}

class LoginOptionsFragment : Fragment(), LoginOptionsView {
    @Inject
    lateinit var presenter: LoginOptionsPresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private var serverName: String? = null
    private var state: String? = null
    private var facebookOauthUrl: String? = null
    private var githubOauthUrl: String? = null
    private var googleOauthUrl: String? = null
    private var linkedinOauthUrl: String? = null
    private var gitlabOauthUrl: String? = null
    private var wordpressOauthUrl: String? = null
    private var casLoginUrl: String? = null
    private var casToken: String? = null
    private var casServiceName: String? = null
    private var casServiceNameTextColor: Int = 0
    private var casServiceButtonColor: Int = 0
    private var customOauthUrl: String? = null
    private var customOauthServiceName: String? = null
    private var customOauthServiceTextColor: Int = 0
    private var customOauthServiceButtonColor: Int = 0
    private var samlUrl: String? = null
    private var samlToken: String? = null
    private var samlServiceName: String? = null
    private var samlServiceTextColor: Int = 0
    private var samlServiceButtonColor: Int = 0
    private var totalSocialAccountsEnabled = 0
    private var isLoginFormEnabled = false
    private var isNewAccountCreationEnabled = false
    private var deepLinkInfo: LoginDeepLinkInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            serverName = getString(SERVER_NAME)
            state = getString(STATE)
            facebookOauthUrl = getString(FACEBOOK_OAUTH_URL)
            githubOauthUrl = getString(GITHUB_OAUTH_URL)
            googleOauthUrl = getString(GOOGLE_OAUTH_URL)
            linkedinOauthUrl = getString(LINKEDIN_OAUTH_URL)
            gitlabOauthUrl = getString(GITLAB_OAUTH_URL)
            wordpressOauthUrl = getString(WORDPRESS_OAUTH_URL)
            casLoginUrl = getString(CAS_LOGIN_URL)
            casToken = getString(CAS_TOKEN)
            casServiceName = getString(CAS_SERVICE_NAME)
            casServiceNameTextColor = getInt(CAS_SERVICE_NAME_TEXT_COLOR)
            casServiceButtonColor = getInt(CAS_SERVICE_BUTTON_COLOR)
            customOauthUrl = getString(CUSTOM_OAUTH_URL)
            customOauthServiceName = getString(CUSTOM_OAUTH_SERVICE_NAME)
            customOauthServiceTextColor = getInt(CUSTOM_OAUTH_SERVICE_NAME_TEXT_COLOR)
            customOauthServiceButtonColor = getInt(CUSTOM_OAUTH_SERVICE_BUTTON_COLOR)
            samlUrl = getString(SAML_URL)
            samlToken = getString(SAML_TOKEN)
            samlServiceName = getString(SAML_SERVICE_NAME)
            samlServiceTextColor = getInt(SAML_SERVICE_NAME_TEXT_COLOR)
            samlServiceButtonColor = getInt(SAML_SERVICE_BUTTON_COLOR)
            totalSocialAccountsEnabled = getInt(TOTAL_SOCIAL_ACCOUNTS)
            isLoginFormEnabled = getBoolean(IS_LOGIN_FORM_ENABLED)
            isNewAccountCreationEnabled = getBoolean(IS_NEW_ACCOUNT_CREATION_ENABLED)
            deepLinkInfo = getParcelable(DEEP_LINK_INFO)
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
        setupAccounts()
        analyticsManager.logScreenView(ScreenViewEvent.LoginOptions)
        deepLinkInfo?.let { presenter.authenticateWithDeepLink(it) }
    }

    private fun setupToolbar() {
        with(activity as AuthenticationActivity) {
            this.clearLightStatusBar()
            toolbar.isVisible = true
            toolbar.title = serverName?.replace(getString(R.string.default_protocol), "")
        }
    }

    private fun setupAccounts() {
        setupSocialAccounts()
        setupCas()
        setupCustomOauth()
        setupSaml()
        setupAccountsView()
        setupLoginWithEmailView()
        setupCreateNewAccountView()
    }

    private fun setupSocialAccounts() {
        if (facebookOauthUrl != null && state != null) {
            setupFacebookButtonListener(facebookOauthUrl.toString(), state.toString())
            enableLoginByFacebook()
        }

        if (githubOauthUrl != null && state != null) {
            setupGithubButtonListener(githubOauthUrl.toString(), state.toString())
            enableLoginByGithub()
        }

        if (googleOauthUrl != null && state != null) {
            setupGoogleButtonListener(googleOauthUrl.toString(), state.toString())
            enableLoginByGoogle()
        }

        if (linkedinOauthUrl != null && state != null) {
            setupLinkedinButtonListener(linkedinOauthUrl.toString(), state.toString())
            enableLoginByLinkedin()
        }


        if (gitlabOauthUrl != null && state != null) {
            setupGitlabButtonListener(gitlabOauthUrl.toString(), state.toString())
            enableLoginByGitlab()
        }

        if (wordpressOauthUrl != null && state != null) {
            setupWordpressButtonListener(wordpressOauthUrl.toString(), state.toString())
            enableLoginByWordpress()
        }
    }

    private fun setupCas() {
        if (casLoginUrl != null && casToken != null && casServiceName != null) {
            addCasButton(
                casLoginUrl.toString(),
                casToken.toString(),
                casServiceName.toString(),
                casServiceNameTextColor,
                casServiceButtonColor
            )
        }
    }

    private fun setupCustomOauth() {
        if (customOauthUrl != null && state != null && customOauthServiceName != null) {
            addCustomOauthButton(
                customOauthUrl.toString(),
                state.toString(),
                customOauthServiceName.toString(),
                customOauthServiceTextColor,
                customOauthServiceButtonColor
            )
        }
    }

    private fun setupSaml() {
        if (samlUrl != null && samlToken != null && samlServiceName != null) {
            addSamlButton(
                samlUrl.toString(),
                samlToken.toString(),
                samlServiceName.toString(),
                samlServiceTextColor,
                samlServiceButtonColor
            )
        }
    }

    private fun setupAccountsView() {
        if (totalSocialAccountsEnabled > 0) {
            showAccountsView()
            if (totalSocialAccountsEnabled > 3) {
                setupExpandAccountsView()
            }
        }
    }

    private fun setupLoginWithEmailView() {
        if (isLoginFormEnabled) {
            showLoginWithEmailButton()
        }
    }

    private fun setupCreateNewAccountView() {
        if (isNewAccountCreationEnabled) {
            showCreateNewAccountButton()
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
    override fun addCasButton(
        caslUrl: String,
        casToken: String,
        serviceName: String,
        serviceNameColor: Int,
        buttonColor: Int
    ) {
        val button = getCustomServiceButton(serviceName, serviceNameColor, buttonColor)
        setupButtonListener(button, caslUrl, casToken, REQUEST_CODE_FOR_CAS)
        accounts_container.addView(button)
    }

    // Custom OAuth account.
    override fun addCustomOauthButton(
        customOauthUrl: String,
        state: String,
        serviceName: String,
        serviceNameColor: Int,
        buttonColor: Int
    ) {
        val button = getCustomServiceButton(serviceName, serviceNameColor, buttonColor)
        setupButtonListener(button, customOauthUrl, state, REQUEST_CODE_FOR_OAUTH)
        accounts_container.addView(button)
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
        setupButtonListener(button, samlUrl, samlToken, REQUEST_CODE_FOR_SAML)
        accounts_container.addView(button)
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
            var isAccountsCollapsed = true
            button_expand_collapse_accounts.setOnClickListener {
                isAccountsCollapsed = if (isAccountsCollapsed) {
                    button_expand_collapse_accounts.rotateBy(180F, 400)
                    expandAccountsView()
                    false
                } else {
                    button_expand_collapse_accounts.rotateBy(180F, 400)
                    collapseAccountsView()
                    true
                }
            }
        }
    }

    override fun showLoginWithEmailButton() {
        ui {
            button_login_with_email.setOnClickListener { presenter.toLoginWithEmail() }
            button_login_with_email.isVisible = true
        }
    }

    override fun showCreateNewAccountButton() {
        ui {
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

        val button = Button(
            ContextThemeWrapper(context, R.style.Authentication_Button),
            null,
            R.style.Authentication_Button
        )
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
            .filter { it.isClickable && !it.isVisible }
            .forEach { it.isVisible = true }
    }

    private fun collapseAccountsView() {
        (0..accounts_container.childCount)
            .mapNotNull { accounts_container.getChildAt(it) as? Button }
            .filter { it.isClickable && it.isVisible }
            .drop(3)
            .forEach { it.isVisible = false }
    }
}
