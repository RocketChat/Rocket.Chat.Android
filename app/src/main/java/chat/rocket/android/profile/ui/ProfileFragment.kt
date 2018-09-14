package chat.rocket.android.profile.ui

import DrawableHelper
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import com.facebook.drawee.backends.pipeline.Fresco
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.Observables
import kotlinx.android.synthetic.main.avatar_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.update_avatar_options.*
import javax.inject.Inject

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

    companion object {
        fun newInstance() = ProfileFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_profile)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupListeners()
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            tintEditTextDrawableStart()
        }
        presenter.loadUserProfile()
        subscribeEditTexts()

        analyticsManager.logScreenView(ScreenViewEvent.Profile)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        unsubscribeEditTexts()
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

    override fun showProfile(avatarUrl: String, name: String, username: String, email: String?) {
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
        Fresco.getImagePipeline().evictFromCache(avatarUrl.toUri())
        image_avatar.setImageURI(avatarUrl)
        (activity as MainActivity).setAvatar(avatarUrl)
    }

    override fun showProfileUpdateSuccessfullyMessage() {
        showMessage(getString(R.string.msg_profile_update_successfully))
    }

    override fun showLoading() {
        enableUserInput(false)
        ui { view_loading.isVisible = true }
    }

    override fun hideLoading() {
        ui {
            if (view_loading != null) {
                view_loading.isVisible = false
            }
        }
        enableUserInput(true)
    }

    override fun showMessage(resId: Int) {
        ui { showToast(resId) }
    }

    override fun showMessage(message: String) {
        ui { showToast(message) }
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
        (activity as AppCompatActivity?)?.supportActionBar?.title =
                getString(R.string.title_profile)
    }

    private fun setupListeners() {
        image_avatar.setOnClickListener { showUpdateAvatarOptions() }

        view_dim.setOnClickListener { hideUpdateAvatarOptions() }

        button_open_gallery.setOnClickListener {
            dispatchImageSelection(REQUEST_CODE_FOR_PERFORM_SAF)
            hideUpdateAvatarOptions()
        }

        button_take_photo.setOnClickListener {
            dispatchTakePicture(REQUEST_CODE_FOR_PERFORM_CAMERA)
            hideUpdateAvatarOptions()
        }

        button_reset_avatar.setOnClickListener {
            hideUpdateAvatarOptions()
            presenter.resetAvatar()
        }
    }

    private fun showUpdateAvatarOptions() {
        view_dim.isVisible = true
        layout_update_avatar_options.isVisible = true
    }

    private fun hideUpdateAvatarOptions() {
        layout_update_avatar_options.isVisible = false
        view_dim.isVisible = false
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
}
