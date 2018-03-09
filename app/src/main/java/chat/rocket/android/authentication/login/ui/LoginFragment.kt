package chat.rocket.android.authentication.login.ui

import DrawableHelper
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ScrollView
import chat.rocket.android.R
import chat.rocket.android.authentication.login.presentation.LoginPresenter
import chat.rocket.android.authentication.login.presentation.LoginView
import chat.rocket.android.helper.AnimationHelper
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.helper.TextHelper
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_log_in.*
import javax.inject.Inject

class LoginFragment : Fragment(), LoginView {
    @Inject lateinit var presenter: LoginPresenter
    @Inject lateinit var appContext: Context // TODO we really need it? Check alternatives...

    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        areLoginOptionsNeeded()
    }

    private var isGlobalLayoutListenerSetUp = false

    companion object {
        fun newInstance() = LoginFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        container?.inflate(R.layout.fragment_authentication_log_in)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        presenter.setup()
        showThreeSocialMethods()

        button_log_in.setOnClickListener {
            presenter.authenticate(text_username_or_email.textContent, text_password.textContent)
        }

        setupFabListener()
        setupSignUpListener()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isGlobalLayoutListenerSetUp) {
            scroll_view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            isGlobalLayoutListenerSetUp = false
        }
    }

    override fun showCasView(casUrl: String, requestedToken: String) {
        activity?.apply {
            // We need to update the view headline, since the CAS allows the log in or sign up
            text_headline.textContent = getString(R.string.title_log_in_or_sign_up)
            setupWebView(casUrl, requestedToken)
        }
    }

    override fun showSignUpView() = text_new_to_rocket_chat.setVisible(true)

    override fun showOauthView() {
        social_accounts_container.setVisible(true)
        button_fab.setVisible(true)

        // We need to setup the layout to hide and show the oauth interface when the soft keyboard is shown
        // (means that the user touched the text_username_or_email or text_password EditText to fill that respective fields).
        if (!isGlobalLayoutListenerSetUp) {
            scroll_view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
            isGlobalLayoutListenerSetUp = true
        }
    }

    override fun showLoginButton() = button_log_in.setVisible(true)

    override fun enableLoginByFacebook() {
        button_facebook.isEnabled = true
    }

    override fun enableLoginByGithub() {
        button_github.isEnabled = true
    }

    override fun enableLoginByGoogle() {
        button_google.isEnabled = true
    }

    override fun enableLoginByLinkedin() {
        button_linkedin.isEnabled = true
    }

    override fun enableLoginByMeteor() {
        button_meteor.isEnabled = true
    }

    override fun enableLoginByTwitter() {
        button_twitter.isEnabled = true
    }

    override fun enableLoginByGitlab() {
        button_gitlab.isEnabled = true
    }

    override fun setupFabListener() {
        button_fab.setOnClickListener({
            button_fab.hide()
            showRemainingSocialAccountsView()
            scrollToBottom()
        })
    }

    override fun hideUsernameOrEmailView() = text_username_or_email.setVisible(false)

    override fun hidePasswordView() = text_password.setVisible(false)

    override fun hideSignUpView() = text_new_to_rocket_chat.setVisible(false)

    override fun hideOauthView() {
        social_accounts_container.setVisible(false)
        button_fab.setVisible(false)
    }

    override fun hideLoginButton() = button_log_in.setVisible(false)

    override fun alertWrongUsernameOrEmail() {
        AnimationHelper.vibrateSmartPhone(appContext)
        AnimationHelper.shakeView(text_username_or_email)
        text_username_or_email.requestFocus()
    }

    override fun alertWrongPassword() {
        AnimationHelper.vibrateSmartPhone(appContext)
        AnimationHelper.shakeView(text_password)
        text_password.requestFocus()
    }

    override fun showLoading() {
        enableUserInput(false)
        view_loading.setVisible(true)
    }

    override fun hideLoading() {
        view_loading.setVisible(false)
        enableUserInput(true)
    }

    override fun showMessage(resId: Int) = showToast(resId)

    override fun showMessage(message: String) = showToast(message)

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showNoInternetConnection() =
        showMessage(getString(R.string.msg_no_internet_connection))

    private fun areLoginOptionsNeeded() {
        if (!isEditTextEmpty() || KeyboardHelper.isSoftKeyboardShown(scroll_view.rootView)) {
            hideSignUpView()
            hideOauthView()
            showLoginButton()
        } else {
            showSignUpView()
            showOauthView()
            hideLoginButton()
        }
    }

    private fun tintEditTextDrawableStart() {
        activity?.apply {
            val personDrawable =
                DrawableHelper.getDrawableFromId(R.drawable.ic_assignment_ind_black_24dp, this)
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, this)

            val drawables = arrayOf(personDrawable, lockDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(arrayOf(text_username_or_email, text_password), drawables)
        }
    }

    private fun setupSignUpListener() {
        val signUp = getString(R.string.title_sign_up)
        val newToRocketChat = String.format(getString(R.string.msg_new_user), signUp)

        text_new_to_rocket_chat.text = newToRocketChat

        val signUpListener = object : ClickableSpan() {
            override fun onClick(view: View) = presenter.signup()
        }

        TextHelper.addLink(text_new_to_rocket_chat, arrayOf(signUp), arrayOf(signUpListener))
    }

    private fun enableUserInput(value: Boolean) {
        button_log_in.isEnabled = value
        text_username_or_email.isEnabled = value
        text_password.isEnabled = value
    }

    // Returns true if *all* EditTexts are empty.
    private fun isEditTextEmpty(): Boolean =
        text_username_or_email.textContent.isBlank() && text_password.textContent.isEmpty()

    private fun showRemainingSocialAccountsView() {
        social_accounts_container.postDelayed({
            for (i in 0..social_accounts_container.childCount) {
                val view = social_accounts_container.getChildAt(i) as? ImageButton ?: continue
                if (view.isEnabled) view.visibility = View.VISIBLE
            }
        }, 1000)
    }

    private fun showThreeSocialMethods() {
        var count = 0
        for (i in 0..social_accounts_container.childCount) {
            val view = social_accounts_container.getChildAt(i) as? ImageButton ?: continue
            if (view.isEnabled && count < 3) {
                view.visibility = View.VISIBLE
                count++
            }
        }
    }

    private fun scrollToBottom() {
        scroll_view.postDelayed({
            scroll_view.fullScroll(ScrollView.FOCUS_DOWN)
        }, 1250)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView(webPageUrl: String, requestedToken: String) {
        web_view.settings.javaScriptEnabled = true
        web_view.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                // The user can be already logged in the CAS, so check if the URL contains "ticket"
                // (that means he/she is successful authenticated and we don't need to wait until the page is finished.
                if (url.contains("ticket")) {
                    authenticateWithCas(requestedToken)
                }
            }

            override fun onPageFinished(view: WebView, url: String) {
                if (url.contains("ticket")) {
                    authenticateWithCas(requestedToken)
                } else {
                    hideLoading()
                    web_view.setVisible(true)
                }
            }
        }
        web_view.loadUrl(webPageUrl)
    }

    private fun authenticateWithCas(requestedToken: String) {
        web_view.setVisible(false)
        presenter.authenticateWithCas(requestedToken)
    }
}