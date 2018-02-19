package chat.rocket.android.chatrooms.ui

import DateTimeHelper
import DrawableHelper
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.core.GlideApp
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.util.extensions.inflate
import chat.rocket.android.util.extensions.setVisible
import chat.rocket.android.util.extensions.textContent
import chat.rocket.android.widget.TextAvatarDrawable
import chat.rocket.common.model.RoomType
import chat.rocket.core.model.ChatRoom
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

        fun bind(chatRoom: ChatRoom) = with(itemView) {
            bindAvatar(chatRoom, /*layout_avatar, */image_room_avatar, text_room_avatar, room_avatar_text)
            bindName(chatRoom, text_chat_name)
            bindLastMessageDateTime(chatRoom, text_last_message_date_time)
            bindLastMessage(chatRoom, text_last_message)
            bindUnreadMessages(chatRoom, text_total_unread_messages)

            setOnClickListener { listener(chatRoom) }
        }

        private fun bindAvatar(chatRoom: ChatRoom, image_room_avatar: ImageView, text_room_avatar: FrameLayout, room_avatar_text: TextView) {
            val chatRoomName = chatRoom.name
            if (chatRoom.type is RoomType.DirectMessage) {
                GlideApp.with(image_room_avatar)
                        .load(UrlHelper.getAvatarUrl(chatRoom.client.url, chatRoomName))
                        .into(image_room_avatar)
            } else {
                image_room_avatar.setImageDrawable(TextAvatarDrawable(chatRoomName.take(1).toUpperCase(),
                        DrawableHelper.getAvatarBackgroundColor(chatRoomName)))
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