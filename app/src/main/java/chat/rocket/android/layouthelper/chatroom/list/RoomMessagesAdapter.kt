package chat.rocket.android.layouthelper.chatroom.list

import android.content.Context
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
import kotlinx.android.synthetic.main.item_room_message.view.*
import kotlinx.android.synthetic.main.day.view.*

class RoomMessagesAdapter(private val dataSet: List<Message>, private val hostname: String, private val context: Context) : RecyclerView.Adapter<RoomMessagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = dataSet[position]

        holder.newDay.text = DateTime.fromEpocMs(message.timestamp, DateTime.Format.DATE)

        val username = message.user?.username
        if (username != null) {
            val placeholderDrawable = UserAvatarHelper.getTextDrawable(username, holder.userAvatar.context)
            holder.userAvatar.loadImage(UserAvatarHelper.getUri(hostname, username), placeholderDrawable)
            holder.username.text = context.getString(R.string.username, username)
        } else {
            holder.userAvatar.visibility = View.GONE
            holder.username.visibility = View.GONE
        }

        holder.messageBody.setText(message.message)
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newDay: TextView = itemView.day
        val userAvatar: RocketChatAvatar = itemView.userAvatar
        val username: TextView = itemView.username
        val messageBody: RocketChatMessageLayout = itemView.messageBody
    }
}