package chat.rocket.android.app

import DrawableHelper
import android.app.Fragment
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import chat.rocket.android.R
import kotlinx.android.synthetic.main.fragment_authentication_sign_up.*

class AuthenticationSignUpFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_authentication_sign_up, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }
    }

    private fun tintEditTextDrawableStart() {
        val context = activity.applicationContext

        val personDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_person_black_24dp, context)
        val atDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_at_black_24dp, context)
        val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, context)
        val emailDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_email_black_24dp, context)

        val drawables = arrayOf(personDrawable, atDrawable, lockDrawable, emailDrawable)
        DrawableHelper.wrapDrawables(drawables)
        DrawableHelper.tintDrawables(drawables, context, R.color.colorDrawableTintGrey)
        DrawableHelper.compoundDrawables(arrayOf(text_name, text_username, text_password, text_email), drawables)
    }
}