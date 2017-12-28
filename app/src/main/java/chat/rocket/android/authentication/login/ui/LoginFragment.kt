package chat.rocket.android.authentication.login.ui

import DrawableHelper
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.text.style.ClickableSpan
import android.view.*
import android.widget.ScrollView
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.authentication.login.presentation.LoginPresenter
import chat.rocket.android.authentication.login.presentation.LoginView
import chat.rocket.android.helper.AnimationHelper
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.helper.TextHelper
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_log_in.*
import javax.inject.Inject

class LoginFragment : Fragment(), LoginView {
    @Inject lateinit var presenter: LoginPresenter
    @Inject lateinit var appContext: Context

    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (KeyboardHelper.isSoftKeyboardShown(scroll_view.rootView)) {
            showOauthView(false)
            showSignUpView(false)
            showLoginButton(true)
        } else {
            if (isEditTextEmpty()) {
                showOauthView(true)
                showSignUpView(true)
                showLoginButton(false)
            }
        }
    }
    private var isGlobalLayoutListenerSetUp = false

    companion object {
        private const val SERVER_URL = "server_url"

        fun newInstance(url: String) = LoginFragment().apply {
            arguments = Bundle(1).apply {
                putString(SERVER_URL, url)
            }
        }
    }

    // Todo remove
    private lateinit var serverUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        // TODO - research a better way to initialize parameters on fragments.
        serverUrl = arguments?.getString(SERVER_URL) ?: "https://open.rocket.chat"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_authentication_log_in, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        // TODO: THIS IS A PRESENTER CONCERN - REMOVE THAT !
        // -------------------------------------------------------------------------------------------------------------------
        showOauthView(true)

        // Show the first three social account's ImageButton (REMARK: we must show at maximum *three* views)
        enableLoginByFacebook()
        enableLoginByGithub()
        enableLoginByGoogle()

        setupFabListener()

        // Just an example: if the server allow the new users registration then show the respective interface.
        setupSignUpListener()
        showSignUpView(true)
        // -------------------------------------------------------------------------------------------------------------------

        button_log_in.setOnClickListener { presenter.authenticate(text_username_or_email.textContent, text_password.textContent) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (isGlobalLayoutListenerSetUp) {
            scroll_view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
            isGlobalLayoutListenerSetUp = false
        }
    }

    override fun showOauthView(value: Boolean) {
        if (value) {
            social_accounts_container.setVisibility(true)
            button_fab.setVisibility(true)

            // We need to setup the layout to hide and show the oauth interface when the soft keyboard is shown
            // (means that the user touched the text_username_or_email or text_password EditText to fill that respective fields).
            if (!isGlobalLayoutListenerSetUp) {
                scroll_view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
                isGlobalLayoutListenerSetUp = true
            }
        } else {
            social_accounts_container.setVisibility(false)
            button_fab.setVisibility(false)
        }
    }

    override fun setupFabListener() {
        button_fab.setOnClickListener({
            button_fab.hide()
            showRemainingSocialAccountsView()
            scrollToBottom()
        })
    }

    override fun enableLoginByFacebook() {
        button_facebook.setVisibility(true)
    }

    override fun enableLoginByGithub() {
        button_github.setVisibility(true)
    }

    override fun enableLoginByGoogle() {
        button_google.setVisibility(true)
    }

    override fun enableLoginByLinkedin() {
        button_linkedin.setVisibility(true)
    }

    override fun enableLoginByMeteor() {
        button_meteor.setVisibility(true)
    }

    override fun enableLoginByTwitter() {
        button_twitter.setVisibility(true)
    }

    override fun enableLoginByGitlab() {
        button_gitlab.setVisibility(true)
    }

    override fun showSignUpView(value: Boolean) {
        text_new_to_rocket_chat.setVisibility(value)
    }

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
        view_loading.show()
    }

    override fun hideLoading() {
        view_loading.hide()
        enableUserInput(true)
    }

    override fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    private fun tintEditTextDrawableStart() {
        activity?.applicationContext?.apply {
            val personDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_assignment_ind_black_24dp, this)
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, this)

            val drawables = arrayOf(personDrawable, lockDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(arrayOf(text_username_or_email, text_password), drawables)
        }
    }

    private fun showLoginButton(value: Boolean) {
        button_log_in.setVisibility(value)
    }

    private fun setupSignUpListener() {
        val signUp = getString(R.string.title_sign_up)
        val newToRocketChat = String.format(getString(R.string.msg_new_to_rocket_chat), signUp)

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
    private fun isEditTextEmpty(): Boolean {
        return text_username_or_email.textContent.isBlank() && text_password.textContent.isEmpty()
    }

    private fun showRemainingSocialAccountsView() {
        social_accounts_container.postDelayed({
            enableLoginByLinkedin()
            enableLoginByMeteor()
            enableLoginByTwitter()
            enableLoginByGitlab()
        }, 1000)
    }

    private fun scrollToBottom() {
        scroll_view.postDelayed({
            scroll_view.fullScroll(ScrollView.FOCUS_DOWN)
        }, 1250)
    }
}