package chat.rocket.android.authentication.ui

import DrawableHelper
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import chat.rocket.android.R
import kotlinx.android.synthetic.main.fragment_authentication_two_fa.*

class TwoFAFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_authentication_two_fa, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }
    }

    private fun tintEditTextDrawableStart() {
        activity?.applicationContext?.apply {
            val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_vpn_key_black_24dp, this)

            DrawableHelper.wrapDrawable(lockDrawable)
            DrawableHelper.tintDrawable(lockDrawable, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawable(text_two_factor_auth, lockDrawable)
        }
    }
}