package chat.rocket.android.chatrooms.ui

import DateTimeHelper
import DrawableHelper
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.util.inflate
import chat.rocket.android.util.setVisibility
import chat.rocket.android.util.textContent
import chat.rocket.common.model.BaseRoom.RoomType
import chat.rocket.core.model.ChatRoom
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.item_chat.view.*

class ChatRoomsAdapter(private val context: Context) : RecyclerView.Adapter<ChatRoomsAdapter.ViewHolder>() {

    var dataSet: MutableList<ChatRoom> = ArrayList()

    fun updateRooms(newRooms: List<ChatRoom>)  {
        dataSet.clear()
        dataSet.addAll(newRooms)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(parent.inflate(R.layout.item_chat))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chatRoom = dataSet[position]
        val chatRoomName = chatRoom.name

        holder.chatName.textContent = chatRoomName

        if (chatRoom.type == RoomType.ONE_TO_ONE) {
            // TODO Check the best way to get the current server url.
            val canonicalUrl = chatRoom.client.url
            holder.userAvatar.setImageURI(UrlHelper.getAvatarUrl(canonicalUrl, chatRoomName))
            holder.userAvatar.setVisibility(true)
        } else {
            holder.roomAvatar.setImageDrawable(DrawableHelper.getTextDrawable(chatRoomName))
            holder.roomAvatar.setVisibility(true)
        }

        val totalUnreadMessage = chatRoom.unread
        when {
            totalUnreadMessage in 1..99 -> {
                holder.unreadMessage.textContent = totalUnreadMessage.toString()
                holder.unreadMessage.setVisibility(true)
            }
            totalUnreadMessage > 99 -> {
                holder.unreadMessage.textContent = context.getString(R.string.msg_more_than_ninety_nine_unread_messages)
                holder.unreadMessage.setVisibility(true)
            }
        }

        val lastMessage = chatRoom.lastMessage
        val lastMessageSender = lastMessage?.sender
        if (lastMessage != null && lastMessageSender != null) {
            val message = lastMessage.message
            val senderUsername = lastMessageSender.username
            when (senderUsername) {
                chatRoomName -> {
                    holder.lastMessage.textContent = message
                }
                // TODO Change to MySelf
//                chatRoom.user?.username -> {
//                    holder.lastMessage.textContent = context.getString(R.string.msg_you) + ": $message"
//                }
                else -> {
                    holder.lastMessage.textContent = "@$senderUsername: $message"
                }
            }
            val localDateTime = DateTimeHelper.getLocalDateTime(lastMessage.timestamp)
            holder.lastMessageDateTime.textContent = DateTimeHelper.getDate(localDateTime, context)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>?) {
        onBindViewHolder(holder, position)
    }

    override fun getItemCount(): Int = dataSet.size

    override fun getItemViewType(position: Int): Int = position

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: SimpleDraweeView = itemView.image_user_avatar
        val roomAvatar: ImageView = itemView.image_room_avatar
        val chatName: TextView = itemView.text_chat_name
        val lastMessage: TextView = itemView.text_last_message
        val lastMessageDateTime: TextView = itemView.text_last_message_date_time
        val unreadMessage: TextView = itemView.text_total_unread_messages
    }
}