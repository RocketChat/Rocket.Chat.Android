package chat.rocket.android.chatroom.adapter

import android.view.View
import chat.rocket.android.chatroom.uimodel.MessageReplyUiModel
import chat.rocket.android.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.item_message_reply.view.*

class MessageReplyViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null,
    private val replyCallback: (roomName: String, permalink: String) -> Unit
) : BaseViewHolder<MessageReplyUiModel>(itemView, listener, reactionListener) {

    init {
        setupActionMenu(itemView)
    }

    override fun bindViews(data: MessageReplyUiModel) {
        with(itemView) {
            button_message_reply.setOnClickListener {
                with(data.rawData) {
                    replyCallback.invoke(roomName, permalink)
                }
            }
        }
    }
}