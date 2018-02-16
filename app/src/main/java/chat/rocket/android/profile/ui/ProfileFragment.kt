package chat.rocket.android.profile.ui

import DrawableHelper
import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v7.view.ActionMode
import android.util.Log
import android.view.*
import android.widget.Toast
import chat.rocket.android.R
import chat.rocket.android.main.ui.MainActivity
import chat.rocket.android.profile.presentation.ProfilePresenter
import chat.rocket.android.profile.presentation.ProfileView
import chat.rocket.android.util.extensions.*
import dagger.android.support.AndroidSupportInjection
import io.reactivex.rxkotlin.Observables
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.app_bar.*
import kotlinx.android.synthetic.main.avatar_profile.*
import kotlinx.android.synthetic.main.fragment_profile.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import javax.inject.Inject


class ProfileFragment : Fragment(), ProfileView, ActionMode.Callback {
    @Inject
    lateinit var presenter: ProfilePresenter
    private lateinit var currentName: String
    private lateinit var currentUsername: String
    private lateinit var currentEmail: String
    private var actionMode: ActionMode? = null
    private var avatarImage: File? = null
    private var isAvatarChanged = false
    //request codes
    private var CHOOSE_PICKER_MODE = 193
    private val CAMERA_REQUEST_CODE = 108
    private val READ_STORAGE_REQUEST_CODE = 109

    private var mObservable: Subject<Boolean> = PublishSubject.create()


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

    override fun showProfile(avatarUrl: String, name: String, username: String, email: String) {
        image_avatar.setImageURI(avatarUrl)

        //click on image_avatar to change avatar
        image_avatar.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                val permissionCheck = ContextCompat.checkSelfPermission(context!!,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                if (permissionCheck != PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), CHOOSE_PICKER_MODE)
                } else {
                    openImagePickerChooserDialog()
                }
            }
        })
        text_name.textContent = name
        text_username.textContent = username
        text_email.textContent = email

        currentName = name
        currentUsername = username
        currentEmail = email

        profile_container.setVisible(true)

        listenToChanges()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            CHOOSE_PICKER_MODE -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImagePickerChooserDialog()
                } else {
                    Toast.makeText(context, getString(R.string.permission_image_picking_not_allowed), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    //show dialog to choose whether to pick image from gallery or to click it from camera
    private fun openImagePickerChooserDialog() {
        val choices = arrayOf("Open camera", "Pick an image from storage")

        val imagePickerChooserDialogBuilder = AlertDialog.Builder(context)
        imagePickerChooserDialogBuilder.setTitle(getString(R.string.title_choose_image_from))
        imagePickerChooserDialogBuilder.setItems(choices, object : DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface?, which: Int) {
                when (which) {
                    0 -> openCamera()
                    1 -> openStorage()
                }
            }
        })
        imagePickerChooserDialogBuilder.show()
    }

    //open camera to capture picture
    private fun openCamera() {
        var intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        avatarImage = createCameraImage()
        startActivityForResult(intent, CAMERA_REQUEST_CODE)

    }

    //open storage to pick image
    private fun openStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("image/*")
        startActivityForResult(intent, READ_STORAGE_REQUEST_CODE)
    }

    private fun createCameraImage(): File {
        val timeStamp = (Date().getTime()).toString()
        val imageFileName = "photo-" + timeStamp

        return File.createTempFile(imageFileName, ".jpg", context!!.cacheDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    //write image data to file
                    //TODO add better method to store image into storage, presently image has a relatively poor quality (added by aniketsingh03)
                    try {
                        val bitmapImage: Bitmap = data!!.extras.get("data") as Bitmap
                        val out = FileOutputStream(avatarImage)
                        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, out)
                        out.close()
                        image_avatar.setImageURI(data.data)
                        isAvatarChanged = true
                        mObservable.onNext(isAvatarChanged)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Log.d("ERROR_CODE", resultCode.toString())
                }
            }

            READ_STORAGE_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    avatarImage = File(data!!.data.path)
                    Toast.makeText(context, data.data.path, Toast.LENGTH_SHORT).show()
                    image_avatar.setImageURI(data.data)
                    isAvatarChanged = true
                    mObservable.onNext(isAvatarChanged)
                } else {
                    Log.d("ERROR_CODE", resultCode.toString())
                }
            }
        }
    }

    override fun showProfileUpdateSuccessfullyMessage() = showMessage(getString(R.string.msg_profile_update_successfully))

    override fun showLoading() {
        enableUserInput(false)
        view_loading.setVisible(true)
    }

    override fun hideLoading() {
        view_loading.setVisible(false)
        enableUserInput(true)
    }

    override fun showMessage(resId: Int) = showToast(resId)

    override fun showMessage(message: String) {
        isAvatarChanged = false
        showToast(message)
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
                presenter.updateUserProfile(text_email.textContent, text_name.textContent, text_username.textContent, avatarImage!!)
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
        (activity as MainActivity).toolbar.title = getString(R.string.title_profile)
    }

    private fun tintEditTextDrawableStart() {
        (activity as MainActivity).apply {
            val personDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_person_black_24dp, this)
            val atDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_at_black_24dp, this)
            val emailDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_email_black_24dp, this)

            val drawables = arrayOf(personDrawable, atDrawable, emailDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(arrayOf(text_name, text_username, text_email), drawables)
        }
    }

    private fun listenToChanges() {
        //setup rx for avatar
        mObservable.subscribe({ _ ->
            if (isAvatarChanged) {
                startActionMode()
            } else {
                finishActionMode()
            }
        })

        Observables.combineLatest(text_name.asObservable(), text_username.asObservable(), text_email.asObservable()).subscribe({ t ->
            if (t.first.toString() != currentName || t.second.toString() != currentUsername || t.third.toString() != currentEmail || isAvatarChanged) {
                startActionMode()
            } else if (!isAvatarChanged) {
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
        text_name.isEnabled = value
        text_username.isEnabled = value
        text_email.isEnabled = value
    }
}
