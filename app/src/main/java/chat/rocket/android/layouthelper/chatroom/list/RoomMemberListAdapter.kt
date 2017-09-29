package chat.rocket.android.layouthelper.chatroom.list

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.widget.RocketChatAvatar
import chat.rocket.android.widget.helper.AvatarHelper
import chat.rocket.android.widget.helper.DrawableHelper
import chat.rocket.core.models.User
import kotlinx.android.synthetic.main.item_room_member.view.*

/**
 * Created by Filipe de Lima Brito (filipedelimabrito@gmail.com) on 9/22/17.
 */
class RoomMemberListAdapter(private var dataSet: List<User>, private val hostname: String, private val context: Context) : RecyclerView.Adapter<RoomMemberListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = dataSet[position]

        holder.name.text = user.name

        val userStatusDrawable: Drawable? = VectorDrawableCompat.create(context.resources, chat.rocket.android.widget.R.drawable.ic_user_status_black_24dp, null)?.mutate()
        DrawableHelper.wrapDrawable(userStatusDrawable)
        when (user.status) {
            User.STATUS_ONLINE -> DrawableHelper.tintDrawable(userStatusDrawable, context, chat.rocket.android.widget.R.color.color_user_status_online)
            User.STATUS_BUSY -> DrawableHelper.tintDrawable(userStatusDrawable, context, chat.rocket.android.widget.R.color.color_user_status_busy)
            User.STATUS_AWAY -> DrawableHelper.tintDrawable(userStatusDrawable, context, chat.rocket.android.widget.R.color.color_user_status_away)
            User.STATUS_OFFLINE -> DrawableHelper.tintDrawable(userStatusDrawable, context, chat.rocket.android.widget.R.color.color_user_status_offline)
        }
        holder.status.setImageDrawable(userStatusDrawable)

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

    fun addDataSet(dataSet: List<User>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet += dataSet
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userAvatar: RocketChatAvatar = itemView.userAvatar
        val name: TextView = itemView.name
        val status: ImageView = itemView.status
        val username: TextView = itemView.username
    }
}