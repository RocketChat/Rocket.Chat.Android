package chat.rocket.android.layouthelper.chatroom.list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.DateTime
import chat.rocket.android.widget.RocketChatAvatar
import chat.rocket.android.widget.helper.UserAvatarHelper
import chat.rocket.android.widget.message.RocketChatMessageLayout
import chat.rocket.core.models.Message
import kotlinx.android.synthetic.main.list_item_message_newday.view.*
import kotlinx.android.synthetic.main.list_item_normal_message.view.*

class RoomPinnedMessagesAdapter(private val dataSet: List<Message>, private val hostname: String) : RecyclerView.Adapter<RoomPinnedMessagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_normal_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = dataSet[position]

        val username = message.user?.username
        if (username != null) {
            val placeholderDrawable = UserAvatarHelper.getTextDrawable(username, holder.userAvatar.context)
            holder.userAvatar.loadImage(UserAvatarHelper.getUri(hostname, username), placeholderDrawable)
            holder.usernameText.text = username
        } else {
            holder.userAvatar.visibility = View.GONE
            holder.usernameText.visibility = View.GONE
        }

        holder.newDayText.text = DateTime.fromEpocMs(message.timestamp, DateTime.Format.DATE)
        holder.messageBody.setText(message.message)
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: RocketChatAvatar = itemView.user_avatar
        val usernameText: TextView = itemView.username
        val messageBody: RocketChatMessageLayout = itemView.message_body
        val newDayText: TextView = itemView.newday_text
    }
}