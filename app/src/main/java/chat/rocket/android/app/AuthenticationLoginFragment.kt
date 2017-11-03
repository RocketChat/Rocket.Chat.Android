package chat.rocket.android.app

import DrawableHelper
import android.app.Fragment
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

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }
    }

    private fun tintEditTextDrawableStart() {
        val context = activity.applicationContext

        val personDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_assignment_ind_black_24dp, context)
        val lockDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_lock_black_24dp, context)

        val drawables = arrayOf(personDrawable, lockDrawable)
        DrawableHelper.wrapDrawables(drawables)
        DrawableHelper.tintDrawables(drawables, context, R.color.colorDrawableTintGrey)
        DrawableHelper.compoundDrawables(arrayOf(text_username, text_password), drawables)
    }
}