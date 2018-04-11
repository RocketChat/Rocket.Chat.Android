package chat.rocket.android.chatroom.adapter

import android.graphics.Color
import android.text.method.LinkMovementMethod
import android.view.View
import chat.rocket.android.chatroom.viewmodel.MessageViewModel
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_message.view.*

class MessageViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null
) : BaseViewHolder<MessageViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            text_content.movementMethod = LinkMovementMethod()
            setupActionMenu(text_content)
        }
    }

    override fun bindViews(data: MessageViewModel) {
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
        }
    }
}