package chat.rocket.android.chatroom.adapter

import android.content.Intent
import android.net.Uri
import android.view.View
import chat.rocket.android.chatroom.viewmodel.GenericFileAttachmentViewModel
import chat.rocket.android.util.extensions.content
import chat.rocket.android.widget.emoji.EmojiReactionListener
import chat.rocket.common.util.ifNull
import kotlinx.android.synthetic.main.item_file_attachment.view.*

class GenericFileAttachmentViewHolder(itemView: View,
                                      listener: ActionsListener,
                                      reactionListener: EmojiReactionListener? = null)
    : BaseViewHolder<GenericFileAttachmentViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(file_attachment_container)
        }
    }

    override fun bindViews(data: GenericFileAttachmentViewModel) {
        with(itemView) {
            text_file_name.content = data.attachmentTitle

            text_file_name.setOnClickListener {
                it.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(data.attachmentUrl)))
            }
        }
    }
}