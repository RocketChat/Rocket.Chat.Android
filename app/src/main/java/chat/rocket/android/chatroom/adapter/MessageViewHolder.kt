package chat.rocket.android.chatroom.adapter

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.core.view.isVisible
import chat.rocket.android.chatroom.uimodel.MessageUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import chat.rocket.core.model.isSystemMessage
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*

class MessageViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null
) : BaseViewHolder<MessageUiModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(message_container)
            text_content.movementMethod = LinkMovementMethod()
        }
    }

    override fun bindViews(data: MessageUiModel) {
        with(itemView) {
            if (data.isFirstUnread) new_messages_notif.visibility = View.VISIBLE
            else new_messages_notif.visibility = View.GONE

            text_message_time.text = data.time
            text_sender.text = data.senderName
            text_content.text = data.content
            image_avatar.setImageURI(data.avatar)
            text_content.setTextColor(
                if (data.isTemporary) Color.GRAY else Color.BLACK
            )
            data.message.let {
                text_edit_indicator.isVisible = !it.isSystemMessage() && it.editedBy != null
                image_star_indicator.isVisible = it.starred?.isNotEmpty() ?: false
            }
        }
    }
}