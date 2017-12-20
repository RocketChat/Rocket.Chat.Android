package chat.rocket.android.authentication.login.ui

import DrawableHelper
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.ScrollView
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.authentication.login.presentation.LoginPresenter
import chat.rocket.android.authentication.login.presentation.LoginView
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_log_in.*
import javax.inject.Inject

class LoginFragment : Fragment(), LoginView {

    companion object {
        private const val SERVER_URL = "server_url"

        fun newInstance(url: String) = LoginFragment().apply {
            arguments = Bundle(1).apply {
                putString(SERVER_URL, url)
            }
        }
    }

    var progress: ProgressDialog? = null
    lateinit var serverUrl: String

    @Inject
    lateinit var presenter: LoginPresenter

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

        // Just an example: if the server allow the login via social accounts (oauth authentication) then show the respective interface.
        shouldShowOauthView(true)
        // In this case we need to setup the layout to hide and show the oauth interface when the soft keyboard is shown (means that the user touched the text_username_or_email and text_password EditText).
        setupGlobalLayoutListener()

        // Show the first three social account's ImageButton (REMARK: we must show at maximum *three* views)
        showLoginUsingFacebookImageButton()
        showLoginUsingGithubImageButton()
        showLoginUsingGoogleImageButton()

        // Setup the FloatingActionButton to show more social account's ImageButton (it expands the social accounts interface to show more views).
        setupFabListener()

        // Just an example: if the server allow the new users registration then show the respective interface.
        shouldShowSignUpMsgView(true)

        button_log_in.setOnClickListener {
            presenter.authenticate(text_username_or_email.text.toString(), text_password.text.toString())
        }

        text_new_to_rocket_chat.setOnClickListener {
            presenter.signup()
        }
    }

    override fun onDestroyView() {
        scroll_view.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        super.onDestroyView()
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

    private fun setupGlobalLayoutListener() {
        scroll_view.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (KeyboardHelper.isSoftKeyboardShown(scroll_view.rootView)) {
            shouldShowOauthView(false)
            shouldShowSignUpMsgView(false)
            shouldShowLoginButton(true)
        } else {
            if (isEditTextNullOrBlank()) {
                shouldShowOauthView(true)
                shouldShowSignUpMsgView(true)
                shouldShowLoginButton(false)
            }
        }
    }

    private fun shouldShowOauthView(show: Boolean) {
        if (show) {
            social_accounts_container.visibility = View.VISIBLE
            button_fab.visibility = View.VISIBLE
        } else {
            social_accounts_container.visibility = View.GONE
            button_fab.visibility = View.GONE
        }
    }

    private fun shouldShowSignUpMsgView(show: Boolean) {
        if (show) {
            text_new_to_rocket_chat.visibility = View.VISIBLE
        } else {
            text_new_to_rocket_chat.visibility = View.GONE
        }
    }

    private fun shouldShowLoginButton(show: Boolean) {
        if (show) {
            button_log_in.visibility = View.VISIBLE
        } else {
            button_log_in.visibility = View.GONE
        }
    }

    private fun showLoginUsingFacebookImageButton() {
        button_facebook.visibility = View.VISIBLE
    }

    private fun showLoginUsingGithubImageButton() {
        button_github.visibility = View.VISIBLE
    }

    private fun showLoginUsingGoogleImageButton() {
        button_google.visibility = View.VISIBLE
    }

    private fun showLoginUsingLinkedinImageButton() {
        button_linkedin.visibility = View.VISIBLE
    }

    private fun showLoginUsingMeteorImageButton() {
        button_meteor.visibility = View.VISIBLE
    }

    private fun showLoginUsingTwitterImageButton() {
        button_twitter.visibility = View.VISIBLE
    }

    private fun showLoginUsingGitlabImageButton() {
        button_gitlab.visibility = View.VISIBLE
    }

    private fun setupFabListener() {
        button_fab.setOnClickListener({
            showLoginUsingLinkedinImageButton()
            showLoginUsingMeteorImageButton()
            showLoginUsingTwitterImageButton()
            showLoginUsingGitlabImageButton()

            scrollToBottom()
            hideFab()
        })
    }

    // Returns true if *all* EditText are null or blank.
    private fun isEditTextNullOrBlank(): Boolean {
        return text_username_or_email.text.isNullOrBlank() && text_password.text.isNullOrBlank()
    }

    private fun scrollToBottom() {
        scroll_view.postDelayed({
            scroll_view.fullScroll(ScrollView.FOCUS_DOWN)
        }, 1000)
    }

    private fun hideFab() {
        button_fab.postDelayed({
            button_fab.hide()
        }, 1500)
    }

    override fun showLoading() {
        // TODO - change for a proper progress indicator
        progress = ProgressDialog.show(activity, "Authenticating",
                "Verifying user credentials", true, true)
    }

    override fun hideLoading() {
        progress?.apply {
            cancel()
        }
        progress = null
    }

    override fun onLoginError(message: String?) {
        // TODO - show a proper error message
        Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
    }
}