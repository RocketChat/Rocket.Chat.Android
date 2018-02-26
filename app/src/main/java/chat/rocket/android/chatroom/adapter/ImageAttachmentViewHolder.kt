package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.R
import chat.rocket.android.chatroom.viewmodel.ImageAttachmentViewModel
import chat.rocket.android.util.extensions.setImageURI
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.message_attachment.view.*

class ImageAttachmentViewHolder(itemView: View) : BaseViewHolder<ImageAttachmentViewModel>(itemView) {
    override fun bindViews(data: ImageAttachmentViewModel) {
        with(itemView) {
            // TODO use data.preview
            image_attachment.setImageURI(data.attachmentUrl) {
                placeholder(R.drawable.image_dummy)
                centerCrop()
                transition(withCrossFade())
            }
            file_name.text = data.attachmentTitle
            image_attachment.setOnClickListener { view ->
                // TODO - implement a proper image viewer with a proper Transition
                /*ImageViewer.Builder(view.context, listOf(data.attachmentUrl))
                        .setStartPosition(0)
                        .show()*/
            }
        }
    }

}