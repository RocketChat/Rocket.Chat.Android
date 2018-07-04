package chat.rocket.android.account.ui

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import chat.rocket.android.R
import chat.rocket.android.account.presentation.AccountPresenter
import chat.rocket.android.account.presentation.AccountView
import chat.rocket.android.util.DrawableHelper
import chat.rocket.android.util.showToast
import chat.rocket.android.util.ui
import chat.rocket.common.model.UserStatus
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.fragment_account.*
import javax.inject.Inject

class AccountFragment : Fragment(), AccountView {

    @Inject
    lateinit var presenter: AccountPresenter

    companion object {
        fun newInstance() = AccountFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater!!.inflate(R.layout.fragment_account, container, false)

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter.loadUserProfile()
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

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun showLoading() {
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui { view_loading.isVisible = false }
    }

    override fun showProfile(
        realName: String,
        userName: String,
        avatarUrl: String,
        status: UserStatus?
    ) {
        ui {
            full_name.text = realName
            user_name.text = String.format(getString(R.string.default_current_user_name), userName)
            user_avatar.setImageURI(avatarUrl)
            user_status_drawable.setImageDrawable(
                DrawableHelper.getUserStatusDrawable(
                    status,
                    context
                )
            )
        }
    }
}