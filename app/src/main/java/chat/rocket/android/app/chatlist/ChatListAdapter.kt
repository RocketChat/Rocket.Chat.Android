package chat.rocket.android.app.chatlist

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import chat.rocket.android.R
import kotlinx.android.synthetic.main.item_chat.view.*

class ChatListAdapter(private var dataSet: List<Chat>, private val context: Context) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = dataSet[position]
        holder.userAvatar.setImageURI(chat.userAvatarUri)
        holder.roomName.text = chat.roomName
        holder.lastMessage.text = chat.lastMessage
        holder.lastMessageTimestamp.text = chat.lastMessageTimestamp

        val unreadMessage = chat.unreadMessage
        when {
            unreadMessage in 1..99 -> {
                holder.unreadMessage.text = unreadMessage.toString()
                holder.unreadMessage.visibility = View.VISIBLE
            }
            unreadMessage > 99 -> {
                holder.unreadMessage.text = context.getString(R.string.msg_more_than_ninety_nine_unread_messages)
                holder.unreadMessage.visibility = View.VISIBLE
            }
            else -> holder.unreadMessage.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar = itemView.image_user_avatar
        val roomName = itemView.text_room_name
        val lastMessage = itemView.text_last_message
        val lastMessageTimestamp = itemView.text_last_message_timestamp
        val unreadMessage = itemView.text_unread_message
    }
}