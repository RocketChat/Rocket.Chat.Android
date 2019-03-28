package chat.rocket.android.authentication.registerusername.ui

import DrawableHelper
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.authentication.registerusername.presentation.RegisterUsernamePresenter
import chat.rocket.android.authentication.registerusername.presentation.RegisterUsernameView
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showKeyboard
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_authentication_register_username.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val BUNDLE_USER_ID = "user_id"
private const val BUNDLE_AUTH_TOKEN = "auth_token"

fun newInstance(userId: String, authToken: String): Fragment = RegisterUsernameFragment().apply {
    arguments = Bundle(2).apply {
        putString(BUNDLE_USER_ID, userId)
        putString(BUNDLE_AUTH_TOKEN, authToken)
    }
}

class RegisterUsernameFragment : Fragment(), RegisterUsernameView {
    @Inject
    lateinit var presenter: RegisterUsernamePresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private lateinit var userId: String
    private lateinit var authToken: String
    private lateinit var usernameDisposable: Disposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidSupportInjection.inject(this)

        arguments?.run {
            userId = getString(BUNDLE_USER_ID, "")
            authToken = getString(BUNDLE_AUTH_TOKEN, "")
        } ?: requireNotNull(arguments) { "no arguments supplied when the fragment was instantiated" }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_authentication_register_username)

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
        subscribeEditText()

        analyticsManager.logScreenView(ScreenViewEvent.RegisterUsername)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeEditText()
    }

    override fun enableButtonUseThisUsername() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_use_this_username, ContextCompat.getColorStateList(it, R.color.colorAccent)
            )
            button_use_this_username.isEnabled = true
        }
    }

    override fun disableButtonUseThisUsername() {
        context?.let {
            ViewCompat.setBackgroundTintList(
                button_use_this_username,
                ContextCompat.getColorStateList(it, R.color.colorAuthenticationButtonDisabled)
            )
            button_use_this_username.isEnabled = false
        }
    }

    override fun showLoading() {
        ui {
            disableUserInput()
            view_loading.isVisible = true
        }
    }

    override fun hideLoading() {
        ui {
            view_loading.isVisible = false
            enableUserInput()
        }
    }

    override fun showMessage(resId: Int) {
        ui {
            showToast(resId)
        }
    }

    override fun showMessage(message: String) {
        ui {
            showToast(message)
        }
    }

    override fun showGenericErrorMessage() {
        showMessage(getString(R.string.msg_generic_error))
    }

    private fun tintEditTextDrawableStart() {
        ui {
            val atDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_at_black_20dp, it)
            DrawableHelper.wrapDrawable(atDrawable)
            DrawableHelper.tintDrawable(atDrawable, it, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawable(text_username, atDrawable)
        }
    }

    private fun enableUserInput() {
        enableButtonUseThisUsername()
        text_username.isEnabled = true
    }

    private fun disableUserInput() {
        disableButtonUseThisUsername()
        text_username.isEnabled = true
    }

    private fun setupOnClickListener() {
        button_use_this_username.setOnClickListener {
            presenter.registerUsername(text_username.textContent, userId, authToken)
        }
    }

    private fun subscribeEditText() {
        usernameDisposable = text_username.asObservable()
            .debounce(300, TimeUnit.MILLISECONDS, AndroidSchedulers.mainThread())
            .subscribe {
                if (it.isNotBlank()) {
                    enableButtonUseThisUsername()
                } else {
                    disableButtonUseThisUsername()
                }
            }
    }

    private fun unsubscribeEditText() = usernameDisposable.dispose()
}