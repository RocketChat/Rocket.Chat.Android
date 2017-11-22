package chat.rocket.android.app.chatlist

import DrawableHelper
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.item_chat.view.*

/**
 * @author Filipe de Lima Brito (filipedelimabrito@gmail.com)
 */
class ChatListAdapter(private var dataSet: MutableList<Chat>, private val context: Context) : RecyclerView.Adapter<ChatListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val chat = dataSet[position]

        holder.userAvatar.setImageURI(chat.userAvatarUri)
        holder.chatName.text = chat.name
        holder.lastMessage.text = chat.lastMessage
        holder.lastMessageTimestamp.text = DateTimeHelper.getDate(chat.lastMessageDateTime, context)

        when (chat.type) {
            "p" -> DrawableHelper.compoundDrawable(holder.chatName, DrawableHelper.getDrawableFromId(R.drawable.ic_lock_outline_black, context))
            "c" -> DrawableHelper.compoundDrawable(holder.chatName, DrawableHelper.getDrawableFromId(R.drawable.ic_hashtag_black, context))
            "d" -> {
                val userStatus = chat.userStatus
                if (userStatus != null) {
                    DrawableHelper.compoundDrawable(holder.chatName, DrawableHelper.getUserStatusDrawable(userStatus, context))
                }
            }
        }

        val totalUnreadMessage = chat.totalUnreadMessages
        when {
            totalUnreadMessage in 1..99 -> {
                holder.unreadMessage.text = totalUnreadMessage.toString()
                holder.unreadMessage.visibility = View.VISIBLE
            }
            totalUnreadMessage > 99 -> {
                holder.unreadMessage.text = context.getString(R.string.msg_more_than_ninety_nine_unread_messages)
                holder.unreadMessage.visibility = View.VISIBLE
            }
        }
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: SimpleDraweeView = itemView.image_user_avatar
        val chatName: TextView = itemView.text_chat_name
        val lastMessage: TextView = itemView.text_last_message
        val lastMessageTimestamp: TextView = itemView.text_last_message_timestamp
        val unreadMessage: TextView = itemView.text_total_unread_messages
    }
}