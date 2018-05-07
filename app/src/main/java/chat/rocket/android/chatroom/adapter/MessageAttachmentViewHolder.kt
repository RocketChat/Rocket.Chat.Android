package chat.rocket.android.chatroom.adapter

import android.text.method.LinkMovementMethod
import android.view.View
import chat.rocket.android.chatroom.viewmodel.MessageAttachmentViewModel
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.item_message_attachment.view.*

class MessageAttachmentViewHolder(
        itemView: View,
        listener: ActionsListener,
        reactionListener: EmojiReactionListener? = null
) : BaseViewHolder<MessageAttachmentViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(attachment_container)
            text_content.movementMethod = LinkMovementMethod()
        }
    }

    override fun bindViews(data: MessageAttachmentViewModel) {
        with(itemView) {
            text_message_time.text = data.time
            text_sender.text = data.senderName
            text_content.text = data.content
        }
    }
}