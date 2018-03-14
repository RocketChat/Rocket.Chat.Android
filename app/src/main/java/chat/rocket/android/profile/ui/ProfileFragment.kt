package chat.rocket.android.profile.ui

import DrawableHelper
import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import javax.inject.Inject


class ProfileFragment : Fragment(), ProfileView, ActionMode.Callback {
    @Inject
    lateinit var presenter: ProfilePresenter
    private lateinit var currentName: String
    private lateinit var currentUsername: String
    private lateinit var currentEmail: String
    private lateinit var currentAvatar: String
    private var actionMode: ActionMode? = null
    private var avatarImage: File? = null
    private var avatarImageUri: Uri? = null
    private var isAvatarChanged = false
    //request codes
    private var CHOOSE_PICKER_MODE = 193
    private val CAMERA_REQUEST_CODE = 108
    private val READ_STORAGE_REQUEST_CODE = 109

    private var imageObservable: Subject<Boolean> = PublishSubject.create()


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
                    ActivityCompat.requestPermissions(activity as FragmentActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), CHOOSE_PICKER_MODE)
                } else {
                    openImagePickerChooserDialog()
                }
            }
        })
        text_name.textContent = name
        text_username.textContent = username
        text_email.textContent = email
        text_avatar_url.textContent = ""

        currentName = username
        currentUsername = name
        currentEmail = email
        currentAvatar = avatarUrl

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
        val choices = arrayOf(getString(R.string.action_open_camera), getString(R.string.action_choose_image_from_gallery))

        val imagePickerChooserDialogBuilder = AlertDialog.Builder(context)
        imagePickerChooserDialogBuilder.setTitle("")
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
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    //open storage to pick image
    private fun openStorage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.setType("image/*")
        startActivityForResult(intent, READ_STORAGE_REQUEST_CODE)
    }

    //create a file in the phone storage for sending to the server later on
    private fun createCameraImage(): File {
        val timeStamp = (Date().getTime()).toString()
        val imageFileName = "photo-" + timeStamp
        val storageDir = context!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpeg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    //write image data to file
                    try {
                        avatarImageUri = data!!.data
                        val bitmapImage: Bitmap = data.extras.get("data") as Bitmap
                        val bytes = ByteArrayOutputStream()
                        bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        image_avatar.setImageURI(data.data)

                        avatarImage = createCameraImage()
                        try {
                            avatarImage!!.createNewFile()

                            val outputStream = FileOutputStream(avatarImage)
                            outputStream.write(bytes.toByteArray())
                            outputStream.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }

                        isAvatarChanged = true
                        imageObservable.onNext(isAvatarChanged)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    Log.d("ERROR_CODE", resultCode.toString())
                }
            }

            READ_STORAGE_REQUEST_CODE -> {
                if (resultCode == RESULT_OK) {
                    avatarImageUri = data!!.data
                    avatarImage = File(data.data.getRealPathFromURI(context!!))
                    image_avatar.setImageURI(data.data)
                    isAvatarChanged = true
                    imageObservable.onNext(isAvatarChanged)
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
                presenter.updateUserProfile(text_email.textContent, text_name.textContent, text_username.textContent, text_avatar_url.textContent, avatarImage, avatarImageUri)

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
            val linkDrawable = DrawableHelper.getDrawableFromId(R.drawable.ic_link_black_24dp, this)

            val drawables = arrayOf(personDrawable, atDrawable, emailDrawable, linkDrawable)
            DrawableHelper.wrapDrawables(drawables)
            DrawableHelper.tintDrawables(drawables, this, R.color.colorDrawableTintGrey)
            DrawableHelper.compoundDrawables(arrayOf(text_name, text_username, text_email, text_avatar_url), drawables)
        }
    }

    private fun listenToChanges() {
        //setup rx for avatar
        imageObservable.subscribe({ _ ->
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
        Observables.combineLatest(text_name.asObservable(),
                text_username.asObservable(),
                text_email.asObservable(),
                text_avatar_url.asObservable()) { text_name, text_username, text_email, text_avatar_url ->
            return@combineLatest (text_name.toString() != currentName ||
                    text_username.toString() != currentUsername ||
                    text_email.toString() != currentEmail ||
                    (text_avatar_url.toString() != "" && text_avatar_url.toString() != currentAvatar))
        }.subscribe({ isValid ->
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
        text_username.isEnabled = value
        text_username.isEnabled = value
        text_email.isEnabled = value
        image_avatar.isEnabled = value
        if (value)
            image_avatar.alpha = 1.0f
        else
            image_avatar.alpha = 0.5f
        text_avatar_url.isEnabled = value

    }
}
