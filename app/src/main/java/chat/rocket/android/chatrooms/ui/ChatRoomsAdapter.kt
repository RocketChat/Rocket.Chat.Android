package chat.rocket.android.chatrooms.ui

import DateTimeHelper
import android.content.Context
import android.support.v4.content.res.ResourcesCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.textContent
import chat.rocket.core.model.ChatRoom
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.avatar.view.*
import kotlinx.android.synthetic.main.item_chat.view.*
import kotlinx.android.synthetic.main.unread_messages_badge.view.*

class ChatRoomsAdapter(private val context: Context,
                       private val listener: (ChatRoom) -> Unit) : RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {
    var dataSet: MutableList<ChatRoom> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_chat))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int = position

    fun updateRooms(newRooms: List<ChatRoom>)  {
        dataSet.clear()
        dataSet.addAll(newRooms)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(chatRoom: ChatRoom) = with(itemView) {
            bindAvatar(chatRoom, image_avatar)
            bindName(chatRoom, text_chat_name)
            bindLastMessageDateTime(chatRoom, text_last_message_date_time)
            bindLastMessage(chatRoom, text_last_message)
            bindUnreadMessages(chatRoom, text_total_unread_messages)

            if (chatRoom.alert || chatRoom.unread > 0) {
                text_chat_name.alpha = 1F
                text_last_message_date_time.setTextColor(ResourcesCompat.getColor(resources, R.color.colorAccent, null))
                text_last_message.setTextColor(ResourcesCompat.getColor(resources, android.R.color.primary_text_light, null))
            }

            setOnClickListener { listener(chatRoom) }
        }

        private fun bindAvatar(chatRoom: ChatRoom, drawee: SimpleDraweeView) {
            drawee.setImageURI(UrlHelper.getAvatarUrl(chatRoom.client.url, chatRoom.name))
        }

        private fun bindName(chatRoom: ChatRoom, textView: TextView) {
            textView.textContent = chatRoom.name
        }

        private fun bindLastMessageDateTime(chatRoom: ChatRoom, textView: TextView) {
            val lastMessage = chatRoom.lastMessage
            if (lastMessage != null) {
                val localDateTime = DateTimeHelper.getLocalDateTime(lastMessage.timestamp)
                textView.textContent = DateTimeHelper.getDate(localDateTime, context)
            }
        }

        private fun bindLastMessage(chatRoom: ChatRoom, textView: TextView) {
            val lastMessage = chatRoom.lastMessage
            val lastMessageSender = lastMessage?.sender
            if (lastMessage != null && lastMessageSender != null) {
                val message = lastMessage.message
                val senderUsername = lastMessageSender.username
                when (senderUsername) {
                    chatRoom.name -> {
                        textView.textContent = message
                    }
                // TODO Change to MySelf
                //                chatRoom.user?.username -> {
                //                    holder.lastMessage.textContent = context.getString(R.string.msg_you) + ": $message"
                //                }
                    else -> {
                        textView.textContent = "@$senderUsername: $message"
                    }
                }
            }
        }

        private fun bindUnreadMessages(chatRoom: ChatRoom, textView: TextView) {
            val totalUnreadMessage = chatRoom.unread
            when {
                totalUnreadMessage in 1..99 -> {
                    textView.textContent = totalUnreadMessage.toString()
                    textView.setVisible(true)
                }
                totalUnreadMessage > 99 -> {
                    textView.textContent = context.getString(R.string.msg_more_than_ninety_nine_unread_messages)
                    textView.setVisible(true)
                }
            }
        }
    }
}