package chat.rocket.android.authentication.signup.ui

import DrawableHelper
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.helper.KeyboardHelper
import chat.rocket.android.authentication.signup.presentation.SignupPresenter
import chat.rocket.android.authentication.signup.presentation.SignupView
import chat.rocket.android.helper.AnimationHelper
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_sign_up.*
import javax.inject.Inject

class SignupFragment : Fragment(), SignupView {
    @Inject lateinit var presenter: SignupPresenter
    lateinit var serverUrl: String
    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener {
        if (KeyboardHelper.isSoftKeyboardShown(constraint_layout.rootView)) {
            text_new_user_agreement.visibility = View.GONE
        } else {
            text_new_user_agreement.visibility = View.VISIBLE
        }
    }

    companion object {
        private const val SERVER_URL = "server_url"

        fun newInstance(url: String) = SignupFragment().apply {
            arguments = Bundle(1).apply {
                putString(SERVER_URL, url)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        // TODO - research a better way to initialize parameters on fragments.
        serverUrl = arguments?.getString(SERVER_URL) ?: "https://open.rocket.chat"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = inflater.inflate(R.layout.fragment_authentication_sign_up, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        setupGlobalLayoutListener()

        button_sign_up.setOnClickListener {
            presenter.signup(text_name, text_username, text_password, text_email)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        constraint_layout.viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
    }


    override fun showLoading() {
        view_loading.setVisibility(true)
    }

    override fun hideLoading() {
        view_loading.setVisibility(false)
    }

    override fun showMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    override fun shakeView(viewToShake: View) {
        AnimationHelper.vibrate(viewToShake.context)
        AnimationHelper.shakeView(viewToShake)
        viewToShake.requestFocus()
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
}