package chat.rocket.android.authentication.login.ui

import DrawableHelper
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.ScrollView
import chat.rocket.android.R
import chat.rocket.android.authentication.login.presentation.LoginPresenter
import chat.rocket.android.authentication.login.presentation.LoginView
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.helper.TextHelper
import chat.rocket.android.util.extensions.*
import chat.rocket.android.webview.cas.ui.webViewIntent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_log_in.*
import javax.inject.Inject

internal const val REQUEST_CODE_FOR_CAS = 1

class LoginFragment : Fragment(), LoginView {
    @Inject lateinit var presenter: LoginPresenter
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

        presenter.setupView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isGlobalLayoutListenerSetUp) {
            scroll_view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            isGlobalLayoutListenerSetUp = false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_FOR_CAS) {
            if (resultCode == Activity.RESULT_OK) {
                data?.apply {
                    presenter.authenticateWithCas(getStringExtra("cas_token"))
                }
            }
        }
    }

    private fun tintEditTextDrawableStart() {
        activity?.apply {
            val personDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_assignment_ind_black_24dp, this)
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, this)

            val drawables = arrayOf(personDrawable, lockDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(arrayOf(text_username_or_email, text_password), drawables)
        }
    }

    override fun showLoading() {
        view_loading.setVisible(true)
    }

    override fun hideLoading() {
        view_loading.setVisible(false)
    }

    override fun showNoInternetConnection() {
        showMessage(R.string.msg_no_internet_connection)
    }

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun showGenericErrorMessage() {
        showMessage(R.string.msg_generic_error)
    }

    override fun showFormView() {
        text_username_or_email.setVisible(true)
        text_password.setVisible(true)
    }

    override fun hideFormView() {
        text_username_or_email.setVisible(false)
        text_password.setVisible(false)
    }

    override fun setupLoginButtonListener() {
        button_log_in.setOnClickListener {
            presenter.authenticate(text_username_or_email.textContent, text_password.textContent)
        }
    }

    override fun enableUserInput() {
        button_log_in.isEnabled = true
        text_username_or_email.isEnabled = true
        text_password.isEnabled = true
    }

    override fun disableUserInput() {
        button_log_in.isEnabled = false
        text_username_or_email.isEnabled = false
        text_password.isEnabled = false
    }

    override fun showCasButton() {
        button_cas.setVisible(true)
    }

    override fun hideCasButton() {
        button_cas.setVisible(false)
    }

    override fun setupCasButtonListener(casUrl: String, casToken: String) {
        button_cas.setOnClickListener {
            startActivityForResult(context?.webViewIntent(casUrl, casToken), REQUEST_CODE_FOR_CAS)
            activity?.overridePendingTransition(R.anim.slide_up, R.anim.hold)
        }
    }

    override fun showSignUpView() {
        text_new_to_rocket_chat.setVisible(true)
    }

    override fun setupSignUpView() {
        val signUp = getString(R.string.title_sign_up)
        val newToRocketChat = String.format(getString(R.string.msg_new_user), signUp)

        text_new_to_rocket_chat.text = newToRocketChat

        val signUpListener = object : ClickableSpan() {
            override fun onClick(view: View) = presenter.signup()
        }

        TextHelper.addLink(text_new_to_rocket_chat, arrayOf(signUp), arrayOf(signUpListener))
    }

    override fun hideSignUpView() {
        text_new_to_rocket_chat.setVisible(false)
    }

    override fun showOauthView() {
        showThreeSocialAccountsMethods()
        social_accounts_container.setVisible(true)
    }

    override fun hideOauthView() {
        social_accounts_container.setVisible(false)
        button_fab.setVisible(false)
    }

    override fun showLoginButton() {
        button_log_in.setVisible(true)
    }

    override fun hideLoginButton() {
        button_log_in.setVisible(false)
    }

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
        button_fab.setVisible(true)
        button_fab.setOnClickListener({
            button_fab.hide()
            showRemainingSocialAccountsView()
            scrollToBottom()
        })
    }

    override fun setupGlobalListener() {
        // We need to setup the layout to hide and show the oauth interface when the soft keyboard is shown
        // (means that the user touched the text_username_or_email or text_password EditText to fill that respective fields).
        if (!isGlobalLayoutListenerSetUp) {
            scroll_view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
            isGlobalLayoutListenerSetUp = true
        }
    }

    override fun alertWrongUsernameOrEmail() {
        vibrateSmartPhone()
        text_username_or_email.shake()
        text_username_or_email.requestFocus()
    }

    override fun alertWrongPassword() {
        vibrateSmartPhone()
        text_password.shake()
        text_password.requestFocus()
    }

    private fun showRemainingSocialAccountsView() {
        social_accounts_container.postDelayed({
            (0..social_accounts_container.childCount)
                .mapNotNull { social_accounts_container.getChildAt(it) as? ImageButton }
                .filter { it.isEnabled }
                .forEach { it.visibility = View.VISIBLE }
        }, 1000)
    }

    // Scrolling to the bottom of the screen.
    private fun scrollToBottom() {
        scroll_view.postDelayed({
            scroll_view.fullScroll(ScrollView.FOCUS_DOWN)
        }, 1250)
    }


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

    // Returns true if *all* EditTexts are empty.
    private fun isEditTextEmpty(): Boolean  {
        return text_username_or_email.textContent.isBlank() && text_password.textContent.isEmpty()
    }

    private fun showThreeSocialAccountsMethods() {
        var count = 0
        for (i in 0..social_accounts_container.childCount) {
            val view = social_accounts_container.getChildAt(i) as? ImageButton ?: continue
            if (view.isEnabled && count < 3) {
                view.visibility = View.VISIBLE
                count++
            }
        }
    }
}