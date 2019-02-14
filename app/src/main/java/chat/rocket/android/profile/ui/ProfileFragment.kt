package chat.rocket.android.profile.ui

import DrawableHelper
import android.app.Activity
import androidx.appcompat.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.MenuInflater
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import chat.rocket.android.R
import chat.rocket.android.analytics.AnalyticsManager
import chat.rocket.android.analytics.event.ScreenViewEvent
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.profile.presentation.ProfilePresenter
import chat.rocket.android.profile.presentation.ProfileView
import chat.rocket.android.util.extension.asObservable
import chat.rocket.android.util.extension.dispatchImageSelection
import chat.rocket.android.util.extension.dispatchTakePicture
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.showToast
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.util.extensions.ui
import chat.rocket.android.util.invalidateFirebaseToken
import com.facebook.drawee.backends.pipeline.Fresco
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.avatar_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.update_avatar_options.*
import javax.inject.Inject

// WIDECHAT
import chat.rocket.android.helper.Constants
import chat.rocket.android.util.extensions.openTabbedUrl
import kotlinx.android.synthetic.main.app_bar.* // need this for back button in setupToolbar
import kotlinx.android.synthetic.main.fragment_profile_widechat.*

internal const val TAG_PROFILE_FRAGMENT = "ProfileFragment"

private const val REQUEST_CODE_FOR_PERFORM_SAF = 1
private const val REQUEST_CODE_FOR_PERFORM_CAMERA = 2

class ProfileFragment : Fragment(), ProfileView, ActionMode.Callback {
    @Inject
    lateinit var presenter: ProfilePresenter
    @Inject
    lateinit var analyticsManager: AnalyticsManager
    private var currentName = ""
    private var currentUsername = ""
    private var currentEmail = ""
    private var actionMode: ActionMode? = null
    private val editTextsDisposable = CompositeDisposable()

    // WIDECHAT
    private var profileFragment: Int = R.layout.fragment_profile_widechat

    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)

        if (!Constants.WIDECHAT) {
            profileFragment = R.layout.fragment_profile
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = container?.inflate(profileFragment)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        if ((Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) && (!Constants.WIDECHAT)) {
            tintEditTextDrawableStart()
        }
        presenter.loadUserProfile()
        if (!Constants.WIDECHAT) {
            subscribeEditTexts()
        }

        analyticsManager.logScreenView(ScreenViewEvent.Profile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!Constants.WIDECHAT) {
            unsubscribeEditTexts()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (resultData != null && resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_FOR_PERFORM_SAF) {
                presenter.updateAvatar(resultData.data)
            } else if (requestCode == REQUEST_CODE_FOR_PERFORM_CAMERA) {
                presenter.preparePhotoAndUpdateAvatar(resultData.extras["data"] as Bitmap)
            }
        }
    }

    fun showWidechatProfile(avatarUrl: String, name: String, username: String, email: String?) {
        ui {
            image_avatar.setImageURI(avatarUrl)
            widechat_text_username.textContent = username
            widechat_text_email.textContent = email ?: ""
            widechat_profile_container.isVisible = true
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        if (actionMode != null) {
            menu.clear()
        }
        super.onPrepareOptionsMenu(menu)
        if (Constants.WIDECHAT) {
            menu.findItem(R.id.action_delete_account).isVisible = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.profile, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_delete_account -> showDeleteAccountDialog()
        }
        return true
    }

    override fun showProfile(avatarUrl: String, name: String, username: String, email: String?) {
        if (Constants.WIDECHAT) {
            showWidechatProfile(avatarUrl, name, username, email)
            return
        }

        ui {
            image_avatar.setImageURI(avatarUrl)
            text_name.textContent = name
            text_username.textContent = username
            text_email.textContent = email ?: ""

            currentName = name
            currentUsername = username
            currentEmail = email ?: ""

            profile_container.isVisible = true
        }
    }

    override fun reloadUserAvatar(avatarUrl: String) {
        Fresco.getImagePipeline().clearCaches()
        image_avatar.setImageURI(avatarUrl)
        if (!Constants.WIDECHAT) {
            (activity as MainActivity).setAvatar(avatarUrl)
        } else {
            presenter.loadUserProfile()
        }
    }

    override fun showProfileUpdateSuccessfullyMessage() {
        showMessage(getString(R.string.msg_profile_update_successfully))
    }

    override fun invalidateToken(token: String) = invalidateFirebaseToken(token)

    override fun showLoading() {
        if (Constants.WIDECHAT) {
            ui { widechat_view_loading.isVisible = true }
        } else {
            enableUserInput(false)
            ui { view_loading.isVisible = true }
        }
    }

    override fun hideLoading() {
        if (Constants.WIDECHAT) {
            ui {
                if (widechat_view_loading != null) {
                    widechat_view_loading.isVisible = false
                }
            }
        } else {
            ui {
                if (view_loading != null) {
                    view_loading.isVisible = false
                }
            }
            enableUserInput(true)
        }
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
    }

    override fun showGenericErrorMessage() = showMessage(getString(R.string.msg_generic_error))

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        mode.menuInflater.inflate(R.menu.action_mode_profile, menu)
        mode.title = getString(R.string.title_update_profile)
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, menuItem: MenuItem): Boolean {
        return when (menuItem.itemId) {
            R.id.action_update_profile -> {
                presenter.updateUserProfile(
                        text_email.textContent,
                        text_name.textContent,
                        text_username.textContent
                )
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
        if (Constants.WIDECHAT) {
            // WIDECHAT - added this to get the back button
            with((activity as MainActivity).toolbar) {
                title = getString(R.string.title_profile)
                setNavigationIcon(R.drawable.ic_arrow_back_white_24dp)
                setNavigationOnClickListener { activity?.onBackPressed() }
            }
            (activity as AppCompatActivity?)?.supportActionBar?.setDisplayShowCustomEnabled(false)

        } else {
            (activity as AppCompatActivity?)?.supportActionBar?.title =
                    getString(R.string.title_profile)
        }
    }

    private fun setupListeners() {
        image_avatar.setOnClickListener { showUpdateAvatarOptions() }

        if (Constants.WIDECHAT) {
            widechat_view_dim.setOnClickListener { hideUpdateAvatarOptions() }

            var onClickCallback = {url: String? ->
                edit_profile_button.setOnClickListener { view: View ->
                    view.openTabbedUrl(url)
                }
                edit_profile_button.setBackgroundResource(R.drawable.widechat_update_profile_button)
            }

            presenter.setUpdateUrl(getString(R.string.widechat_sso_profile_update_path), onClickCallback)

            delete_account_button.setOnClickListener { showToast("Delete Account Button Clicked") }
        } else {
            view_dim.setOnClickListener { hideUpdateAvatarOptions() }
        }

        button_open_gallery.setOnClickListener {
            dispatchImageSelection(REQUEST_CODE_FOR_PERFORM_SAF)
            hideUpdateAvatarOptions()
        }

        button_take_a_photo.setOnClickListener {
            dispatchTakePicture(REQUEST_CODE_FOR_PERFORM_CAMERA)
            hideUpdateAvatarOptions()
        }

        button_reset_avatar.setOnClickListener {
            hideUpdateAvatarOptions()
            presenter.resetAvatar()
        }
    }

    private fun showUpdateAvatarOptions() {
        if (Constants.WIDECHAT) {
            widechat_view_dim.isVisible = true
            widechat_layout_update_avatar_options.isVisible = true
        } else {
            view_dim.isVisible = true
            layout_update_avatar_options.isVisible = true
        }
    }

    private fun hideUpdateAvatarOptions() {
        if (Constants.WIDECHAT) {
            widechat_layout_update_avatar_options.isVisible = false
            widechat_view_dim.isVisible = false
        } else {
            layout_update_avatar_options.isVisible = false
            view_dim.isVisible = false
        }
    }

    private fun tintEditTextDrawableStart() {
        (activity as MainActivity).apply {
            val personDrawable =
                    DrawableHelper.getDrawableFromId(R.drawable.ic_person_black_20dp, this)
            val atDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_at_black_20dp, this)
            val emailDrawable =
                    DrawableHelper.getDrawableFromId(R.drawable.ic_email_black_20dp, this)

            val drawables = arrayOf(personDrawable, atDrawable, emailDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(
                    arrayOf(text_name, text_username, text_email), drawables
            )
        }
    }

    private fun subscribeEditTexts() {
        editTextsDisposable.add(Observables.combineLatest(
                text_name.asObservable(),
                text_username.asObservable(),
                text_email.asObservable()
        ) { text_name, text_username, text_email ->
            return@combineLatest (text_name.toString() != currentName ||
                    text_username.toString() != currentUsername ||
                    text_email.toString() != currentEmail)
        }.subscribe { isValid ->
            activity?.invalidateOptionsMenu()
            if (isValid) {
                startActionMode()
            } else {
                finishActionMode()
            }
        })
    }

    private fun unsubscribeEditTexts() = editTextsDisposable.clear()

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
        }
    }

    fun showDeleteAccountDialog() {
        val passwordEditText = EditText(context)
        passwordEditText.hint = getString(R.string.msg_password)

        context?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle(R.string.title_are_you_sure)
                .setView(passwordEditText)
                .setPositiveButton(R.string.action_delete_account) { _, _ ->
                    presenter.deleteAccount(passwordEditText.text.toString())
                }
                .setNegativeButton(android.R.string.no) { dialog, _ -> dialog.cancel() }
                .create()
                .show()
        }
    }
}
