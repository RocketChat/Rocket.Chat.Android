package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.chatroom.viewmodel.ImageAttachmentViewModel
import chat.rocket.android.helper.ImageHelper
import chat.rocket.android.widget.emoji.EmojiReactionListener
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.message_attachment.view.*

class ImageAttachmentViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null
) : BaseViewHolder<ImageAttachmentViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(attachment_container)
        }
    }

    override fun bindViews(data: ImageAttachmentViewModel) {
        with(itemView) {
            val controller = Fresco.newDraweeControllerBuilder().apply {
                setUri(data.attachmentUrl)
                autoPlayAnimations = true
                oldController = image_attachment.controller
            }.build()
            image_attachment.controller = controller
            file_name.text = data.attachmentTitle
            image_attachment.setOnClickListener {
                ImageHelper.openImage(
                    it.context,
                    data.attachmentUrl,
                    data.attachmentTitle.toString()
                )
            }
        }
    }
}