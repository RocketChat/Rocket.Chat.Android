package chat.rocket.android.chatrooms.ui

import DateTimeHelper
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setImageURI
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.widget.AvatarTextDrawable
import chat.rocket.common.model.RoomType
import chat.rocket.core.model.ChatRoom
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import kotlinx.android.synthetic.main.item_chat.view.*

class ChatRoomsAdapter(private val context: Context,
                       private val listener: (ChatRoom) -> Unit) : RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {
    var dataSet: MutableList<ChatRoom> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_chat))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(dataSet[position])

    override fun getItemCount(): Int = dataSet.size

    fun updateRooms(newRooms: List<ChatRoom>)  {
        dataSet.clear()
        dataSet.addAll(newRooms)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val placeholder = AvatarTextDrawable()

        fun bind(chatRoom: ChatRoom) = with(itemView) {
            bindAvatar(chatRoom, image_room_avatar)
            bindName(chatRoom, text_chat_name)
            bindLastMessageDateTime(chatRoom, text_last_message_date_time)
            bindLastMessage(chatRoom, text_last_message)
            bindUnreadMessages(chatRoom, text_total_unread_messages)

            setOnClickListener { listener(chatRoom) }
        }

        private fun bindAvatar(chatRoom: ChatRoom, image_room_avatar: ImageView) {
            val avatarId = if (chatRoom.type is RoomType.DirectMessage) chatRoom.name else chatRoom.id
            image_room_avatar
                    .setImageURI(UrlHelper.getAvatarUrl(chatRoom.client.url, avatarId)) {
                        placeholder(placeholder)
                        transition(withCrossFade())
                    }
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
            } else {
                textView.textContent = ""
            }
        }

        private fun bindUnreadMessages(chatRoom: ChatRoom, textView: TextView) {
            val totalUnreadMessage = chatRoom.unread
            when {
                totalUnreadMessage == 0L -> textView.setVisible(false)
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