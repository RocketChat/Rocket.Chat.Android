package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.chatroom.uimodel.ImageAttachmentUiModel
import chat.rocket.android.helper.ImageHelper
import chat.rocket.android.emoji.EmojiReactionListener
import com.facebook.drawee.backends.pipeline.Fresco
import kotlinx.android.synthetic.main.message_attachment.view.*

class ImageAttachmentViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null
) : BaseViewHolder<ImageAttachmentUiModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(attachment_container)
        }
    }

    override fun bindViews(data: ImageAttachmentUiModel) {
        with(itemView) {
            val controller = Fresco.newDraweeControllerBuilder().apply {
                setUri(data.attachmentUrl)
                autoPlayAnimations = true
                oldController = image_attachment.controller
            }.build()
            image_attachment.controller = controller
            file_name.text = data.attachmentTitle
            file_description.text = data.attachmentDescription
            file_text.text = data.attachmentText
            image_attachment.setOnClickListener {
                ImageHelper.openImage(
                    context,
                    data.attachmentUrl,
                    data.attachmentTitle.toString()
                )
            }
        }
    }
}