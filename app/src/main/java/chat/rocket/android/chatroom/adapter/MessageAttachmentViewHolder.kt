package chat.rocket.android.chatroom.adapter

import android.text.method.LinkMovementMethod
import android.view.View
import chat.rocket.android.chatroom.uimodel.MessageAttachmentUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.item_message_attachment.view.*

class MessageAttachmentViewHolder(
        itemView: View,
        listener: ActionsListener,
        reactionListener: EmojiReactionListener? = null
) : BaseViewHolder<MessageAttachmentUiModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(attachment_container)
            text_content.movementMethod = LinkMovementMethod()
        }
    }

    override fun bindViews(data: MessageAttachmentUiModel) {
        with(itemView) {
            text_message_time.text = data.time
            text_sender.text = data.senderName
            text_content.text = data.content
        }
    }
}