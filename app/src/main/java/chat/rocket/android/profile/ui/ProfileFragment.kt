package chat.rocket.android.profile.ui

import DrawableHelper
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.view.ActionMode
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.profile.presentation.ProfilePresenter
import chat.rocket.android.profile.presentation.ProfileView
import chat.rocket.android.util.extensions.*
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.avatar_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import javax.inject.Inject

class ProfileFragment : Fragment(), ProfileView, ActionMode.Callback {
    @Inject lateinit var presenter: ProfilePresenter
    private lateinit var currentName: String
    private lateinit var currentUsername: String
    private lateinit var currentEmail: String
    private lateinit var currentAvatar: String
    private var actionMode: ActionMode? = null
    private val disposables = CompositeDisposable()

    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = container?.inflate(R.layout.fragment_profile)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }

        presenter.loadUserProfile()
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }

    override fun showProfile(avatarUrl: String, name: String, username: String, email: String?) {
        ui {
            image_avatar.setImageURI(avatarUrl)

            text_name.textContent = name
            text_username.textContent = username
            text_email.textContent = email ?: ""
            text_avatar_url.textContent = ""

            currentName = name
            currentUsername = username
            currentEmail = email ?: ""
            currentAvatar = avatarUrl

            profile_container.setVisible(true)

            listenToChanges()
        }
    }

    override fun showProfileUpdateSuccessfullyMessage() {
        showMessage(getString(R.string.msg_profile_update_successfully))
    }

    override fun showLoading() {
        enableUserInput(false)
        ui {
            view_loading.setVisible(true)
        }
    }

    override fun hideLoading() {
        ui {
            if (view_loading != null) {
                view_loading.setVisible(false)
            }
        }
        enableUserInput(true)
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

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.profile, menu)
        mode.title = getString(R.string.title_update_profile)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_profile -> {
                presenter.updateUserProfile(text_email.textContent, text_name.textContent, text_username.textContent, text_avatar_url.textContent)
                mode.finish()
                true
            }
            else -> {
                false
            }
        }
    }

    override fun onDestroyActionMode(mode: ActionMode) {
        actionMode = null
    }

    private fun setupToolbar() {
        (activity as AppCompatActivity?)?.supportActionBar?.title = getString(R.string.title_profile)
    }

    private fun tintEditTextDrawableStart() {
        (activity as MainActivity).apply {
            val personDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_person_black_24dp, this)
            val atDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_at_black_24dp, this)
            val emailDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_email_black_24dp, this)
            val linkDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_link_black_24dp, this)

            val drawables = arrayOf(personDrawable, atDrawable, emailDrawable, linkDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(arrayOf(text_name, text_username, text_email, text_avatar_url), drawables)
        }
    }

    private fun listenToChanges() {
        disposables.add(Observables.combineLatest(text_name.asObservable(),
                text_username.asObservable(),
                text_email.asObservable(),
                text_avatar_url.asObservable()) { text_name, text_username, text_email, text_avatar_url ->
            return@combineLatest (text_name.toString() != currentName ||
                    text_username.toString() != currentUsername ||
                    text_email.toString() != currentEmail ||
                    (text_avatar_url.toString() != "" && text_avatar_url.toString() != currentAvatar))
        }.subscribe { isValid ->
            if (isValid) {
                startActionMode()
            } else {
                finishActionMode()
            }
        })
    }

    private fun startActionMode() {
        if (actionMode == null) {
            actionMode = (activity as MainActivity).startSupportActionMode(this)
        }
    }

    private fun finishActionMode() = actionMode?.finish()

    private fun enableUserInput(value: Boolean) {
        ui {
            text_username.isEnabled = value
            text_username.isEnabled = value
            text_email.isEnabled = value
            text_avatar_url.isEnabled = value
        }
    }
}
