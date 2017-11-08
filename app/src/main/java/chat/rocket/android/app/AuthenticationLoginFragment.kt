package chat.rocket.android.app

import DrawableHelper
import android.app.Fragment
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import kotlinx.android.synthetic.main.fragment_authentication_log_in.*

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
class AuthenticationLoginFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_authentication_log_in, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) tintEditTextDrawableStart()
        setupEditTextListener()
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)

        if (KeyboardHelper.isHardKeyboardShown(newConfig)) {
            shouldShowSocialAccountsAndSignUpMsgViews(false)
        } else {
            if (isEditTextNullOrBlank()) {
                shouldShowSocialAccountsAndSignUpMsgViews(true)
            }
        }
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
                shouldShowSocialAccountsAndSignUpMsgViews(false)
            } else {
                if (isEditTextNullOrBlank()) {
                    shouldShowSocialAccountsAndSignUpMsgViews(true)
                }
            }
        })

        text_password.viewTreeObserver.addOnGlobalLayoutListener({
            if (KeyboardHelper.isSoftKeyboardShown(text_username_or_email.rootView)) {
                shouldShowSocialAccountsAndSignUpMsgViews(false)
            } else {
                if (isEditTextNullOrBlank()) {
                    shouldShowSocialAccountsAndSignUpMsgViews(true)
                }
            }
        })
    }

    private fun shouldShowSocialAccountsAndSignUpMsgViews(show: Boolean) {
        if (show) {
            social_accounts_container.visibility = View.VISIBLE
            text_new_in_rocket_chat.visibility = View.VISIBLE
            button_fab.visibility = View.VISIBLE
            button_log_in.visibility = View.GONE
        } else {
            social_accounts_container.visibility = View.GONE
            text_new_in_rocket_chat.visibility = View.GONE
            button_fab.visibility = View.GONE
            button_log_in.visibility = View.VISIBLE
        }
    }

    // Returns true if *all* EditText are null or blank.
    private fun isEditTextNullOrBlank(): Boolean {
        return text_username_or_email.text.isNullOrBlank() && text_password.text.isNullOrBlank()
    }
}