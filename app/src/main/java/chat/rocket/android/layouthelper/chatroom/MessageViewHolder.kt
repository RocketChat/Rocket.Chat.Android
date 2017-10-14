package chat.rocket.android.layouthelper.chatroom

import android.support.constraint.ConstraintLayout
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.renderer.MessageRenderer
import chat.rocket.android.widget.RocketChatAvatar
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout
import chat.rocket.android.widget.message.RocketChatMessageLayout
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout
import chat.rocket.core.SyncState
import kotlinx.android.synthetic.main.day.view.*
import kotlinx.android.synthetic.main.item_room_message.view.*

class MessageViewHolder(itemView: View, private val hostname: String, private val viewType: Int) : ModelViewHolder<PairedMessage>(itemView) {
    private val dayLayout: View by lazy { itemView.dayLayout }
    private val day: TextView by lazy { itemView.day }
    private val avatar: RocketChatAvatar by lazy { itemView.avatar }
    private val middleContainer: ConstraintLayout by lazy { itemView.middleContainer }
    private val realName: TextView by lazy { itemView.realName }
    private val username: TextView by lazy { itemView.username }
    private val timestamp: TextView by lazy { itemView.timestamp }
    private val errorImage: ImageView by lazy { itemView.errorImage }
    private val rocketChatMessageLayout: RocketChatMessageLayout by lazy { itemView.messageBody }
    private val rocketChatMessageUrlsLayout: RocketChatMessageUrlsLayout by lazy { itemView.messageUrl }
    private val rocketChatMessageAttachmentsLayout: RocketChatMessageAttachmentsLayout by lazy { itemView.messageAttachment }
    private val messageSystemBody: TextView by lazy { itemView.messageSystemBody }

    override fun bind(model: PairedMessage, autoLoadImage: Boolean) {
        val target = model.target
        val messageRenderer = MessageRenderer(target, autoLoadImage)

        messageRenderer.showAvatar(avatar, hostname)
        messageRenderer.showRealName(realName)
        messageRenderer.showUsername(username)
        messageRenderer.showMessageTimestamp(timestamp)

        if (viewType == MessageListAdapter.VIEW_TYPE_NORMAL_MESSAGE) {
            messageRenderer.showBody(rocketChatMessageLayout)
            messageRenderer.showUrl(rocketChatMessageUrlsLayout)
            messageRenderer.showAttachment(rocketChatMessageAttachmentsLayout)
        } else if (viewType == MessageListAdapter.VIEW_TYPE_SYSTEM_MESSAGE) {
            messageRenderer.showSystemBody(messageSystemBody)
        }

        if (!model.hasSameDate()) {
            messageRenderer.showNewDay(dayLayout, day)
        } else {
            messageRenderer.hideViews(arrayOf(dayLayout))
        }

        if (target.syncState == SyncState.FAILED) {
            messageRenderer.showErrorView(errorImage)
        }
    }
}