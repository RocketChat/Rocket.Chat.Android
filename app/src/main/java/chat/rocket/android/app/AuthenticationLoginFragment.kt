package chat.rocket.android.app

import DrawableHelper
import android.app.Fragment
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ScrollView
import chat.rocket.android.R
import kotlinx.android.synthetic.main.fragment_authentication_log_in.*

class AuthenticationLoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_authentication_log_in, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        // Just an example: if the server allow the login via social accounts (oauth authentication) then show the respective interface.
        shouldShowOauthView(true)
        // In this case we need to setup the text_username_or_email and text_password EditText to hide and show the oauth interface when the user touch the respective fields.
        setupEditTextListener()

        // Show the first three social account's ImageButton (REMARK: we must show at maximum *three* views)
        showLoginUsingFacebookImageButton()
        showLoginUsingGithubImageButton()
        showLoginUsingGoogleImageButton()

        // Setup the FloatingActionButton to show more social account's ImageButton (it expands the social accounts interface to show more views).
        setupFabListener()

        // Just an example: if the server allow the new users registration then show the respective interface.
        shouldShowSignUpMsgView(true)
    }

    private fun tintEditTextDrawableStart() {
        val context = activity.applicationContext

        val personDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_assignment_ind_black_24dp, context)
        val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, context)

        val drawables = arrayOf(personDrawable, lockDrawable)
        DrawableHelper.wrapDrawables(drawables)
        DrawableHelper.tintDrawables(drawables, context, R.color.colorDrawableTintGrey)
        DrawableHelper.compoundDrawables(arrayOf(text_username_or_email, text_password), drawables)
    }

    private fun setupEditTextListener() {
        text_username_or_email.viewTreeObserver.addOnGlobalLayoutListener({
            if (KeyboardHelper.isSoftKeyboardShown(text_username_or_email.rootView)) {
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
        })

        text_password.viewTreeObserver.addOnGlobalLayoutListener({
            if (KeyboardHelper.isSoftKeyboardShown(text_password.rootView)) {
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
        })
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
}