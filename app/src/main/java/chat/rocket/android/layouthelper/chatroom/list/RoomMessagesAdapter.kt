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
import chat.rocket.android.widget.helper.AvatarHelper
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout
import chat.rocket.android.widget.message.RocketChatMessageLayout
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout
import chat.rocket.core.models.Message
import kotlinx.android.synthetic.main.day.view.*
import kotlinx.android.synthetic.main.item_room_message.view.*

/**
 * Created by Filipe de Lima Brito (filipedelimabrito@gmail.com) on 9/22/17.
 */
class RoomMessagesAdapter(private var dataSet: List<Message>, private val hostname: String, private val context: Context) : RecyclerView.Adapter<RoomMessagesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_room_message, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = dataSet[position]

        holder.newDay.text = DateTime.fromEpocMs(message.timestamp, DateTime.Format.DATE)

        val user = message.user
        if (user != null) {
            if (user.name.isNullOrBlank()) {
                holder.name.visibility = View.GONE
            } else {
                holder.name.text = message.user?.name
            }

            val username = user.username
            if (username != null) {
                val placeholderDrawable = AvatarHelper.getTextDrawable(username, holder.userAvatar.context)
                holder.userAvatar.loadImage(AvatarHelper.getUri(hostname, username), placeholderDrawable)
                holder.username.text = context.getString(R.string.username, username)
            } else {
                holder.userAvatar.visibility = View.GONE
                holder.username.visibility = View.GONE
            }
        }

        holder.messageBody.setText(message.message)

        val webContents = message.webContents
        if (webContents == null || webContents.isEmpty()) {
            holder.messageUrl.visibility = View.GONE
        } else {
            holder.messageUrl.setUrls(message.webContents, true)
        }

        val attachments = message.attachments
        if (attachments == null || attachments.isEmpty()) {
            holder.messageAttachment.visibility = View.GONE
        } else {
//            holder.messageAttachment.setAbsoluteUrl(absoluteUrl)
//            holder.messageAttachment.setAttachments(attachments, true)
//            holder.messageAttachment.visibility = View.VISIBLE
        }
    }

    override fun getItemCount(): Int = dataSet.size

    fun addDataSet(dataSet: List<Message>) {
        val previousDataSetSize = this.dataSet.size
        this.dataSet += dataSet
        notifyItemRangeInserted(previousDataSetSize, dataSet.size)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val newDay: TextView = itemView.day
        val userAvatar: RocketChatAvatar = itemView.userAvatar
        val username: TextView = itemView.username
        val name: TextView = itemView.name
        val messageBody: RocketChatMessageLayout = itemView.messageBody
        val messageUrl: RocketChatMessageUrlsLayout = itemView.messageUrl
        val messageAttachment: RocketChatMessageAttachmentsLayout = itemView.messageAttachment
    }
}