package chat.rocket.android.chatroom.ui

import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.view.isVisible
import chat.rocket.android.util.extensions.getFileName
import chat.rocket.android.util.extensions.getMimeType

fun ChatRoomFragment.showFileAttachmentDialog(uri: Uri) {
    activity?.let { fragmentActivity ->
        uri.getMimeType(fragmentActivity).let { mimeType ->
            description.text.clear()
            when {
                mimeType.startsWith("image") -> {
                    imagePreview.isVisible = true
                    imagePreview.setImageURI(uri)
                }
                mimeType.startsWith("video") -> {
                    audioVideoAttachment.isVisible = true
                }
                else -> {
                    textFile.isVisible = true
                    textFile.text = uri.getFileName(fragmentActivity)
                }
            }
        }
    }

    sendButton.setOnClickListener {
        presenter.uploadFile(chatRoomId, uri, (citation ?: "") + description.text.toString())
        alertDialog.dismiss()
    }
    cancelButton.setOnClickListener { alertDialog.dismiss() }
    alertDialog.show()
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
        alertDialog.dismiss()
    }

    cancelButton.setOnClickListener { alertDialog.dismiss() }
    alertDialog.show()
}