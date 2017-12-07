package chat.rocket.android.authentication.ui

import DrawableHelper
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import chat.rocket.android.R
import chat.rocket.android.app.KeyboardHelper
import kotlinx.android.synthetic.main.fragment_authentication_sign_up.*

class SignUpFragment : Fragment() {

    companion object {
        fun newInstance() = SignUpFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_authentication_sign_up, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        setupGlobalLayoutListener()
    }

    override fun onDestroyView() {
        constraint_layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        super.onDestroyView()
    }

    private fun tintEditTextDrawableStart() {
        activity?.applicationContext?.apply {
            val personDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_person_black_24dp, this)
            val atDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_at_black_24dp, this)
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, this)
            val emailDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_email_black_24dp, this)

            val drawables = arrayOf(personDrawable, atDrawable, lockDrawable, emailDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(arrayOf(text_name, text_username, text_password, text_email), drawables)
        }
    }

    private fun setupGlobalLayoutListener() {
        constraint_layout.viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
    }

    val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (KeyboardHelper.isSoftKeyboardShown(constraint_layout.rootView)) {
            text_new_user_agreement.visibility = View.GONE
        } else {
            text_new_user_agreement.visibility = View.VISIBLE
        }
    }
}