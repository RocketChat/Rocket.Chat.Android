package chat.rocket.android.chatrooms.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import chat.rocket.android.R
import chat.rocket.android.util.DrawableHelper
import chat.rocket.common.model.RoomType
import chat.rocket.core.model.ChatRoom
import kotlinx.android.synthetic.main.item_chat_rooms.view.*

class ChatRoomsAdapter(
    private val context: Context,
    private val chatRoom: List<ChatRoom>
) :
    RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {
    private val singleChatRoom: List<ChatRoom>

    init {
        singleChatRoom = chatRoom
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_chat_rooms,
                parent,
                false
            )
        )

    override fun getItemCount(): Int {
        return chatRoom.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) =
        holder.bind(singleChatRoom[position])

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatRoom: ChatRoom) = with(itemView) {
            bindName(chatRoom, channel_name)
            bindIcon(chatRoom, chat_rooms_icon)
            bindUnreadMessages(chatRoom, text_total_unread_messages)
        }

        fun bindName(chatRoom: ChatRoom, textView: TextView) {
            textView.text = chatRoom.name
            if (chatRoom.alert || chatRoom.unread > 0) {
                textView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorPrimaryText
                    )
                )
            } else {
                textView.setTextColor(
                    ContextCompat.getColor(
                        context,
                        R.color.colorSecondaryText
                    )
                )
            }
        }

        fun bindIcon(chatRoom: ChatRoom, imageView: ImageView) {
            val drawable = when (chatRoom.type) {
                is RoomType.Channel -> DrawableHelper.getDrawableFromId(
                    R.drawable.ic_hashtag_white_12dp,
                    context
                )
                is RoomType.PrivateGroup -> DrawableHelper.getDrawableFromId(
                    R.drawable.ic_lock_white_12dp,
                    context
                )
                is RoomType.DirectMessage -> DrawableHelper.getUserStatusDrawable(
                    chatRoom.status,
                    context
                )
                else -> null
            }
            drawable?.let {
                val mutateDrawable = DrawableHelper.wrapDrawable(it).mutate()
                if (chatRoom.type !is RoomType.DirectMessage) {
                    val color = when (chatRoom.alert || chatRoom.unread > 0) {
                        true -> R.color.colorPrimaryText
                        false -> R.color.colorSecondaryText
                    }
                    DrawableHelper.tintDrawable(mutateDrawable, context, color)
                }
                imageView.setImageDrawable(mutateDrawable)
            }
        }

        private fun bindUnreadMessages(chatRoom: ChatRoom, textView: TextView) {
            val totalUnreadMessage = chatRoom.unread
            when {
                totalUnreadMessage in 1..99 -> {
                    textView.text = totalUnreadMessage.toString()
                    textView.visibility = View.VISIBLE
                }
                totalUnreadMessage > 99 -> {
                    textView.text =
                            context.getString(R.string.msg_more_than_ninety_nine_unread_messages)
                    textView.visibility = View.VISIBLE
                }
                else -> textView.visibility = View.GONE
            }
        }
    }
}