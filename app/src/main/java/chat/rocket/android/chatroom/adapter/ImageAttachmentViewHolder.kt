package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.chatroom.viewmodel.ImageAttachmentViewModel
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import chat.rocket.android.widget.emoji.EmojiReactionListener
import com.stfalcon.frescoimageviewer.ImageViewer
import kotlinx.android.synthetic.main.message_attachment.view.*

class ImageAttachmentViewHolder(itemView: View,
                                listener: ActionsListener,
                                reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<ImageAttachmentViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(attachment_container)
            setupActionMenu(image_attachment)
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
            image_attachment.setOnClickListener { view ->
                // TODO - implement a proper image viewer with a proper Transition
                val builder = ImageViewer.createPipelineDraweeControllerBuilder()
                        .setAutoPlayAnimations(true)
                ImageViewer.Builder(view.context, listOf(data.attachmentUrl))
                        .setStartPosition(0)
                        .hideStatusBar(false)
                        .setCustomDraweeControllerBuilder(builder)
                        .show()
            }
        }
    }

}