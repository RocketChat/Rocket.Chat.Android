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
import kotlinx.android.synthetic.main.fragment_authentication_two_fa.*

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
class AuthenticationTwoFAFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater?.inflate(R.layout.fragment_authentication_two_fa, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }
    }

    private fun tintEditTextDrawableStart() {
        val context = activity.applicationContext

        val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_vpn_key_black_24dp, context)

        DrawableHelper.wrapDrawable(lockDrawable)
        DrawableHelper.tintDrawable(lockDrawable, context, R.color.colorDrawableTintGrey)
        DrawableHelper.compoundDrawable(text_two_factor_auth, lockDrawable)
    }
}