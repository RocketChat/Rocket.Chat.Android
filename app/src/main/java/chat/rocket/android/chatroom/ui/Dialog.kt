package chat.rocket.android.chatroom.ui

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.view.isVisible
import chat.rocket.android.util.extensions.getFileName
import chat.rocket.android.util.extensions.getMimeType
import chat.rocket.common.util.ifNull
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition

fun ChatRoomFragment.showFileAttachmentDialog(uri: Uri) {
    imagePreview.isVisible = false
    audioVideoAttachment.isVisible = false
    textFile.isVisible = false
    lateinit var mimeType: String
    var bitmap: Bitmap? = null

    activity?.let { context ->
        uri.getMimeType(context).let {
            mimeType = it
            description.text.clear()
            when {
                mimeType.startsWith("image") -> {
                    if (mimeType.contains("gif")) {
                        Glide
                            .with(context)
                            .asGif()
                            .load(uri)
                            .apply(RequestOptions().fitCenter())
                            .into(imagePreview)
                    } else {
                        Glide
                            .with(context)
                            .asBitmap()
                            .load(uri)
                            .apply(RequestOptions().fitCenter())
                            .into(object : SimpleTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    bitmap = resource
                                    imagePreview.setImageBitmap(resource)
                                }
                            })
                    }
                    imagePreview.isVisible = true
                }
                mimeType.startsWith("video") -> audioVideoAttachment.isVisible = true
                else -> {
                    textFile.isVisible = true
                    textFile.text = uri.getFileName(context)
                }
            }
        }
    }

    sendButton.setOnClickListener {
        bitmap?.let { bitmap ->
            presenter.uploadImage(
                chatRoomId,
                mimeType,
                uri,
                bitmap,
                (citation ?: "") + description.text.toString()
            )
        }.ifNull {
            presenter.uploadFile(
                chatRoomId,
                mimeType,
                uri,
                (citation ?: "") + description.text.toString()
            )
        }
        alertDialog?.dismiss()
    }
    cancelButton.setOnClickListener { alertDialog?.dismiss() }
    alertDialog?.show()
}

fun ChatRoomFragment.showDrawAttachmentDialog(byteArray: ByteArray) {
    description.text.clear()
    imagePreview.isVisible = true
    imagePreview.setImageDrawable(Drawable.createFromStream(byteArray.inputStream(), ""))

    sendButton.setOnClickListener {
        presenter.uploadDrawingImage(
            chatRoomId,
            byteArray,
            (citation ?: "") + description.text.toString()
        )
        alertDialog?.dismiss()
    }

    cancelButton.setOnClickListener { alertDialog?.dismiss() }
    alertDialog?.show()
}
