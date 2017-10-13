package chat.rocket.android.layouthelper.chatroom

import android.view.View
import android.widget.TextView

import chat.rocket.android.renderer.MessageRenderer
import kotlinx.android.synthetic.main.item_room_message.view.*

class MessageSystemViewHolder(itemView: View, hostname: String) : AbstractMessageViewHolder(itemView, hostname) {
    private val messageSystemBody: TextView = itemView.messageSystemBody

    override fun bindMessage(pairedMessage: PairedMessage, autoLoadImage: Boolean) {
        val messageRenderer = MessageRenderer(pairedMessage.target, autoLoadImage)
        messageRenderer.showAvatar(avatar, hostname)
        messageRenderer.showRealName(realName)
        messageRenderer.showUsername(username)
        messageRenderer.showMessageTimestamp(timestamp)
        messageRenderer.showSystemBody(messageSystemBody)
    }
}