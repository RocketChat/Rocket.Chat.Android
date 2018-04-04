package chat.rocket.android.authentication.registerusername.ui

import DrawableHelper
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import chat.rocket.android.authentication.registerusername.presentation.RegisterUsernamePresenter
import chat.rocket.android.authentication.registerusername.presentation.RegisterUsernameView
import chat.rocket.android.util.extensions.*
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_authentication_register_username.*
import javax.inject.Inject

class RegisterUsernameFragment : Fragment(), RegisterUsernameView {
    @Inject lateinit var presenter: RegisterUsernamePresenter
    private lateinit var userId: String
    private lateinit var authToken: String

    companion object {
        private const val USER_ID = "user_id"
        private const val AUTH_TOKEN = "auth_token"

        fun newInstance(userId: String, authToken: String) = RegisterUsernameFragment().apply {
            arguments = Bundle(1).apply {
                putString(USER_ID, userId)
                putString(AUTH_TOKEN, authToken)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        // TODO - research a better way to initialize parameters on fragments.
        userId = arguments?.getString(USER_ID) ?: ""
        authToken = arguments?.getString(AUTH_TOKEN) ?: ""
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_authentication_register_username)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.apply {
            text_username.requestFocus()
            showKeyboard(text_username)
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        setupOnClickListener()
    }

    override fun alertBlankUsername() {
        vibrateSmartPhone()
        text_username.shake()
    }

    override fun showLoading() {
        disableUserInput()
        view_loading.setVisible(true)
    }

    override fun hideLoading() {
        view_loading.setVisible(false)
        enableUserInput()
    }

    override fun showMessage(resId: Int) {
        showToast(resId)
    }

    override fun showMessage(message: String) {
        showToast(message)
    }

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    override fun showNoInternetConnection() {
        showMessage(getString(R.string.msg_no_internet_connection))
    }

    private fun tintEditTextDrawableStart() {
        activity?.apply {
            val atDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_at_black_24dp, this)
            DrawableHelper.wrapDrawable(atDrawable)
            DrawableHelper.tintDrawable(atDrawable, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawable(text_username, atDrawable)
        }
    }

    private fun enableUserInput() {
        button_use_this_username.isEnabled = true
        text_username.isEnabled = true
    }

    private fun disableUserInput() {
        button_use_this_username.isEnabled = false
        text_username.isEnabled = true
    }

    private fun setupOnClickListener() {
        button_use_this_username.setOnClickListener {
            presenter.registerUsername(text_username.textContent, userId, authToken)
        }
    }
}