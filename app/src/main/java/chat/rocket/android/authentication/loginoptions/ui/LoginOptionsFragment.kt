package chat.rocket.android.authentication.loginoptions.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.authentication.login.ui.REQUEST_CODE_FOR_OAUTH
import chat.rocket.android.authentication.loginoptions.presentation.LoginOptionsPresenter
import chat.rocket.android.authentication.loginoptions.presentation.LoginOptionsView
import chat.rocket.android.authentication.ui.AuthenticationActivity
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.webview.oauth.ui.INTENT_OAUTH_CREDENTIAL_SECRET
import chat.rocket.android.webview.oauth.ui.INTENT_OAUTH_CREDENTIAL_TOKEN
import chat.rocket.android.webview.oauth.ui.oauthWebViewIntent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.app_bar_chat_room.*
import kotlinx.android.synthetic.main.fragment_authentication_login_options.*
import javax.inject.Inject


class LoginOptionsFragment : Fragment(), LoginOptionsView {

    @Inject
    lateinit var presenter: LoginOptionsPresenter

    companion object {
        fun newInstance() = LoginOptionsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_authentication_login_options, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.setupView()
        setupToolbar()
        setupClickListener()
    }

    private fun setupClickListener() {
        button_create_account.setOnClickListener {
            presenter.toCreateAccount()
        }
        button_login.setOnClickListener {
            presenter.toLogin()
        }
    }

    private fun setupToolbar() {
        val toolbar = (activity as AuthenticationActivity).toolbar
        val textServerName = (activity as AuthenticationActivity).text_room_name
        textServerName.text = "open.rocket.chat"
        toolbar.isVisible = true
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
        toolbar.setNavigationOnClickListener {
            (activity as AuthenticationActivity).onBackPressed()
        }
    }

    override fun enableLoginByFacebook() {
        ui{
            button_facebook.isClickable = true
        }
    }

    override fun setupFacebookButtonListener(facebookOauthUrl: String, state: String) {
        ui { activity ->
            button_facebook.setOnClickListener {
                startActivityForResult(
                        activity.oauthWebViewIntent(facebookOauthUrl, state),
                        REQUEST_CODE_FOR_OAUTH
                )
                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
        }
    }

    override fun enableLoginByGithub() {
        ui {
            button_github.isClickable = true
        }
    }

    override fun setupGithubButtonListener(githubUrl: String, state: String) {
        ui { activity ->
            button_github.setOnClickListener {
                startActivityForResult(
                        activity.oauthWebViewIntent(githubUrl, state),
                        REQUEST_CODE_FOR_OAUTH
                )
                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
        }
    }

    override fun enableLoginByGoogle() {
        ui {
            button_google.isClickable = true
        }
    }

    override fun setupGoogleButtonListener(googleUrl: String, state: String) {
        ui { activity ->
            button_google.setOnClickListener {
                startActivityForResult(
                        activity.oauthWebViewIntent(googleUrl, state),
                        REQUEST_CODE_FOR_OAUTH
                )
                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
        }
    }

    override fun enableLoginByLinkedin() {
        ui {
            button_linkedin.isClickable = true
        }
    }

    override fun setupLinkedinButtonListener(linkedinUrl: String, state: String) {
        ui { activity ->
            button_linkedin.setOnClickListener {
                startActivityForResult(
                        activity.oauthWebViewIntent(linkedinUrl, state),
                        REQUEST_CODE_FOR_OAUTH
                )
                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
        }
    }

    override fun enableLoginByGitlab() {
        ui {
            button_gitlab.isClickable = true
        }
    }

    override fun setupGitlabButtonListener(gitlabUrl: String, state: String) {
        ui { activity ->
            button_gitlab.setOnClickListener {
                startActivityForResult(
                        activity.oauthWebViewIntent(gitlabUrl, state),
                        REQUEST_CODE_FOR_OAUTH
                )
                activity.overridePendingTransition(R.anim.slide_up, R.anim.hold)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null){
            when(requestCode){
                REQUEST_CODE_FOR_OAUTH -> {
                    presenter.authenticateWithOauth(
                            data.getStringExtra(INTENT_OAUTH_CREDENTIAL_TOKEN),
                            data.getStringExtra(INTENT_OAUTH_CREDENTIAL_SECRET)
                    )
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
}
