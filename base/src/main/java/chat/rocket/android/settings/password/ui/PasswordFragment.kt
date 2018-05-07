package chat.rocket.android.settings.password.ui

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.settings.password.presentation.PasswordPresenter
import chat.rocket.android.settings.password.presentation.PasswordView
import chat.rocket.android.util.extensions.asObservable
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.textContent
import android.support.v7.view.ActionMode
import chat.rocket.android.util.extensions.ui
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.fragment_password.*
import javax.inject.Inject

class PasswordFragment: Fragment(), PasswordView, android.support.v7.view.ActionMode.Callback {
    @Inject lateinit var presenter: PasswordPresenter
    private var actionMode: ActionMode? = null
    private val disposables = CompositeDisposable()

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

        disposables.add(listenToChanges())
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    override fun hideLoading() {
        ui {
            layout_new_password.visibility = View.VISIBLE
            layout_confirm_password.visibility = View.VISIBLE
            view_loading.visibility = View.GONE
        }
    }

    override fun onActionItemClicked(mode: ActionMode, menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_password -> {
                presenter.updatePassword(text_new_password.textContent)
                mode.finish()
                return true
            }
            else -> {
                false
            }
        }
    }

    override fun onCreateActionMode(mode: ActionMode, menu: Menu?): Boolean {
        mode.menuInflater.inflate(R.menu.password, menu)
        mode.title = resources.getString(R.string.action_confirm_password)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean = false

    override fun onDestroyActionMode(mode: ActionMode?) {
        actionMode = null
    }

    override fun showLoading() {
        ui {
            layout_new_password.visibility = View.GONE
            layout_confirm_password.visibility = View.GONE
            view_loading.visibility = View.VISIBLE
        }
    }

    override fun showPasswordFailsUpdateMessage(error: String?) {
        showToast("Password fails to update: " + error)
    }

    override fun showPasswordSuccessfullyUpdatedMessage() {
        showToast("Password was successfully updated!")
    }

    private fun finishActionMode() = actionMode?.finish()

    private fun listenToChanges(): Disposable {
        return Observables.combineLatest(text_new_password.asObservable(),
                text_confirm_password.asObservable()).subscribe {
            val textPassword = text_new_password.textContent
            val textConfirmPassword = text_confirm_password.textContent

            if (textPassword.length > 5 && textConfirmPassword.length > 5 && textPassword.equals(textConfirmPassword))
                startActionMode()
            else
                finishActionMode()
        }
    }

    private fun showToast(msg: String?) {
        ui {
            Toast.makeText(it, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun startActionMode() {
        if (actionMode == null) {
            actionMode = (activity as PasswordActivity).startSupportActionMode(this)
        }
    }
}