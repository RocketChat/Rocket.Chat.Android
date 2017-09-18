package chat.rocket.android.layouthelper.chatroom.list

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.widget.RocketChatAvatar
import chat.rocket.android.widget.helper.AvatarHelper
import chat.rocket.core.models.User
import kotlinx.android.synthetic.main.item_room_member.view.*

class RoomMemberListAdapter(private val dataSet: List<User>, private val hostname: String, private val context: Context) : RecyclerView.Adapter<RoomMemberListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = dataSet[position]

        val username = user.username
        if (username != null) {
            holder.username.text = context.getString(R.string.username, username)
            val placeholderDrawable = AvatarHelper.getTextDrawable(username, holder.userAvatar.context)
            holder.userAvatar.loadImage(AvatarHelper.getUri(hostname, username), placeholderDrawable)
        } else {
            holder.userAvatar.visibility = View.GONE
            holder.username.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = dataSet.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: RocketChatAvatar = itemView.userAvatar
        val username: TextView = itemView.username
    }
}