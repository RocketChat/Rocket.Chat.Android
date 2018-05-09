package chat.rocket.android.settings.password.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.settings.password.presentation.PasswordPresenter
import chat.rocket.android.settings.password.presentation.PasswordView
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.fragment_password.*
import javax.inject.Inject

class PasswordFragment: Fragment(), PasswordView{
    @Inject lateinit var presenter: PasswordPresenter

    companion object {
        fun newInstance() = PasswordFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_password)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        button_save_password.setOnClickListener{
            val newPassword = editText_new_password.text.toString()
            val confirmPassword = editText_confirm_password.text.toString()
            if (newPassword != "" && newPassword == confirmPassword){
                presenter.updatePassword(newPassword)
            }else if (newPassword != confirmPassword){
                showToast(resources.getString(R.string.msg_password_dont_match))
            }else if (newPassword == ""){
                showToast(resources.getString(R.string.msg_password_empty))
            }
        }
    }

    override fun hideLoading() {
        ui {
            button_save_password.text = resources.getString(R.string.action_save_changes)
            button_save_password.isEnabled = true
            view_loading.visibility = View.GONE
        }
    }

    override fun showLoading() {
        ui {
            button_save_password.text = ""
            button_save_password.isEnabled = false
            view_loading.visibility = View.VISIBLE
        }
    }

    override fun showPasswordFailsUpdateMessage(error: String?) {
        showToast(resources.getString(R.string.msg_password_update_failed,error))
    }

    override fun showPasswordSuccessfullyUpdatedMessage() {
        showToast(resources.getString(R.string.msg_password_update_successful))
    }

    private fun showToast(msg: String?) {
        ui {
            Toast.makeText(it, msg, Toast.LENGTH_LONG).show()
        }
    }
}