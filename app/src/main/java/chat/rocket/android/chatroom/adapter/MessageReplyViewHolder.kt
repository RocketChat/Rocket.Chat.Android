package chat.rocket.android.chatroom.adapter

import android.support.v7.widget.RecyclerView
import android.view.View
import chat.rocket.android.chatroom.viewmodel.MessageReplyViewModel
import chat.rocket.android.util.extensions.isBroadcastReplyAvailable
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.widget.emoji.EmojiReactionListener
import kotlinx.android.synthetic.main.item_message_reply.view.*

class MessageReplyViewHolder(
    itemView: View,
    listener: ActionsListener,
    reactionListener: EmojiReactionListener? = null,
    private val replyCallback: (roomName: String, permalink: String) -> Unit
) : BaseViewHolder<MessageReplyViewModel>(itemView, listener, reactionListener) {

    init {
        with(itemView) {
            setupActionMenu(itemView)
        }
    }

    override fun bindViews(data: MessageReplyViewModel) {
        with(itemView) {
//            if (data.isBroadcastReplyAvailable()) {
//                setVisible(true)
                button_message_reply.setOnClickListener {
                    with(data.rawData) {
                        replyCallback.invoke(roomName, permalink)
                    }
                }
//            } else {
//                visibility = View.GONE
//                itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
//            }
        }
    }
}