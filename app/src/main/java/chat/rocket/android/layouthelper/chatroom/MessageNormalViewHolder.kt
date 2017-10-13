package chat.rocket.android.layouthelper.chatroom

import android.view.View

import chat.rocket.android.renderer.MessageRenderer
import chat.rocket.android.widget.AbsoluteUrl
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout
import chat.rocket.android.widget.message.RocketChatMessageLayout
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout
import kotlinx.android.synthetic.main.item_room_message.view.*

class MessageNormalViewHolder(itemView: View, hostname: String) : AbstractMessageViewHolder(itemView, hostname) {
    private val rocketChatMessageLayout: RocketChatMessageLayout = itemView.messageBody
    private val rocketChatMessageUrlsLayout: RocketChatMessageUrlsLayout = itemView.messageUrl
    private val rocketChatMessageAttachmentsLayout: RocketChatMessageAttachmentsLayout = itemView.messageAttachment

    override fun bindMessage(pairedMessage: PairedMessage, autoLoadImage: Boolean) {
        val messageRenderer = MessageRenderer(pairedMessage.target, autoLoadImage)
        messageRenderer.showAvatar(avatar, hostname)
        messageRenderer.showRealName(realName)
        messageRenderer.showUsername(username)
        messageRenderer.showMessageTimestamp(timestamp)
        messageRenderer.showBody(rocketChatMessageLayout)
        messageRenderer.showUrl(rocketChatMessageUrlsLayout)
        messageRenderer.showAttachment(rocketChatMessageAttachmentsLayout)
    }
}