package chat.rocket.android.helper

import android.Manifest
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.media.MediaScannerConnection
import android.os.Environment
import android.text.TextUtils
import android.util.TypedValue
import android.view.ContextThemeWrapper
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import androidx.core.view.setPadding
import chat.rocket.android.R
import com.facebook.binaryresource.FileBinaryResource
import com.facebook.cache.common.CacheKey
import com.facebook.imageformat.ImageFormatChecker
import com.facebook.imagepipeline.cache.DefaultCacheKeyFactory
import com.facebook.imagepipeline.core.ImagePipelineFactory
import com.facebook.imagepipeline.request.ImageRequest
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.google.android.material.appbar.AppBarLayout
import com.stfalcon.frescoimageviewer.ImageViewer
import timber.log.Timber
import java.io.File

object ImageHelper {
    private var cacheKey: CacheKey? = null

    // TODO - implement a proper image viewer with a proper Transition
    // TODO - We should definitely write our own ImageViewer
    fun openImage(context: Context, imageUrl: String, imageName: String? = "") {
        var imageViewer: ImageViewer? = null
        val request =
            ImageRequestBuilder.newBuilderWithSource(imageUrl.toUri())
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.DISK_CACHE)
                .build()

        cacheKey = DefaultCacheKeyFactory.getInstance()
            .getEncodedCacheKey(request, null)
        val pad = context.resources
            .getDimensionPixelSize(R.dimen.viewer_toolbar_padding)

        val lparams = AppBarLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        val toolbar = Toolbar(context).also {
            it.inflateMenu(R.menu.image_actions)
            it.setOnMenuItemClickListener {
                return@setOnMenuItemClickListener when (it.itemId) {
                    R.id.action_save_image -> saveImage(context)
                    else -> true
                }
            }

            val titleSize = context.resources
                .getDimensionPixelSize(R.dimen.viewer_toolbar_title)
            val titleTextView = TextView(context).also {
                it.text = imageName
                it.setTextColor(Color.WHITE)
                it.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize.toFloat())
                it.ellipsize = TextUtils.TruncateAt.END
                it.setSingleLine()
                it.typeface = Typeface.DEFAULT_BOLD
                it.setPadding(pad)
            }

            val backArrowView = ImageView(context).also {
                it.setImageResource(R.drawable.ic_arrow_back_white_24dp)
                it.setOnClickListener { imageViewer?.onDismiss() }
                it.setPadding(0, pad, pad, pad)
            }

            val layoutParams = AppBarLayout.LayoutParams(
                AppBarLayout.LayoutParams.WRAP_CONTENT,
                AppBarLayout.LayoutParams.WRAP_CONTENT
            )

            it.addView(backArrowView, layoutParams)
            it.addView(titleTextView, layoutParams)
        }

        val appBarLayout = AppBarLayout(context).also {
            it.layoutParams = lparams
            it.setBackgroundColor(Color.BLACK)
            it.addView(
                toolbar, AppBarLayout.LayoutParams(
                    AppBarLayout.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
        }

        val builder = ImageViewer.createPipelineDraweeControllerBuilder()
            .setImageRequest(request)
            .setAutoPlayAnimations(true)

        imageViewer = ImageViewer.Builder(context, listOf(imageUrl))
            .setOverlayView(appBarLayout)
            .setStartPosition(0)
            .hideStatusBar(false)
            .setCustomDraweeControllerBuilder(builder)
            .show()
    }

    private fun saveImage(context: Context): Boolean {
        if (!canWriteToExternalStorage(context)) {
            checkWritingPermission(context)
            return false
        }
        if (ImagePipelineFactory.getInstance().mainFileCache.hasKey(cacheKey)) {
            val resource = ImagePipelineFactory.getInstance().mainFileCache.getResource(cacheKey)
            val cachedFile = (resource as FileBinaryResource).file
            val imageFormat = ImageFormatChecker.getImageFormat(resource.openStream())
            val imageDir = "${Environment.DIRECTORY_PICTURES}/Rocket.Chat Images/"
            val imagePath = Environment.getExternalStoragePublicDirectory(imageDir)
            val imageFile =
                File(imagePath, "${cachedFile.nameWithoutExtension}.${imageFormat.fileExtension}")
            imagePath.mkdirs()
            imageFile.createNewFile()
            try {
                cachedFile.copyTo(imageFile, true)
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(imageFile.absolutePath),
                    null
                ) { path, uri ->
                    Timber.i("Scanned $path:")
                    Timber.i("-> uri=$uri")
                }
            } catch (ex: Exception) {
                Timber.e(ex)
                val message = context.getString(R.string.msg_image_saved_failed)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            } finally {
                val message = context.getString(R.string.msg_image_saved_successfully)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
        return true
    }

    fun canWriteToExternalStorage(context: Context): Boolean {
        return AndroidPermissionsHelper.checkPermission(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun checkWritingPermission(context: Context) {
        if (context is ContextThemeWrapper) {
            val activity = if (context.baseContext is Activity) context.baseContext as Activity else context as Activity
            AndroidPermissionsHelper.requestPermission(
                activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                AndroidPermissionsHelper.WRITE_EXTERNAL_STORAGE_CODE
            )
        }
    }
}