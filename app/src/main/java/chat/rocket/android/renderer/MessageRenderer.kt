package chat.rocket.android.renderer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.DateTime
import chat.rocket.android.layouthelper.chatroom.MessageType
import chat.rocket.android.widget.RocketChatAvatar
import chat.rocket.android.widget.helper.AvatarHelper
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout
import chat.rocket.android.widget.message.RocketChatMessageLayout
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout
import chat.rocket.core.SyncState
import chat.rocket.core.models.Message

class MessageRenderer(private val message: Message, private val autoLoadImage: Boolean) {

    /**
     * Shows the avatar image on the RocketChatAvatar widget.
     */
    fun showAvatar(rocketChatAvatarWidget: RocketChatAvatar, hostname: String) {
        val username: String? = message.user?.username
        if (username != null) {
            val placeholderDrawable = AvatarHelper.getTextDrawable(username, rocketChatAvatarWidget.context)
            if (message.avatar != null) {
                // Load user's avatar image from Oauth provider URI.
                rocketChatAvatarWidget.loadImage(message.avatar, placeholderDrawable)
            } else {
                rocketChatAvatarWidget.loadImage(AvatarHelper.getUri(hostname, username), placeholderDrawable)
            }
        } else {
            rocketChatAvatarWidget.visibility = View.GONE
        }
    }

    /**
     * Shows the user real name on the TextView.
     */
    fun showRealName(textView: TextView) {
        val realName: String? = message.user?.name
        if (realName != null) {
            textView.text = realName
        } else {
            textView.visibility = View.GONE
        }
    }

    /**
     * Shows the username on the TextView.
     */
    fun showUsername(textView: TextView) {
        val username: String? = message.user?.username
        if (username != null) {
            textView.text = textView.context.getString(R.string.sub_username, username)
        } else {
            textView.visibility = View.GONE
        }
    }

    /**
     * Shows message timestamp.
     */
    fun showMessageTimestamp(timestamp: TextView) {
        if (message.syncState == SyncState.SYNCED) {
            timestamp.text = DateTime.fromEpocMs(message.timestamp, DateTime.Format.TIME)
            timestamp.visibility = View.VISIBLE
        } else {
            timestamp.visibility = View.GONE
        }
    }

    /**
     * Shows the system's message on the TextView.
     */
    fun showSystemBody(textView: TextView) {
        textView.text = MessageType.parse(message.type).getString(textView.context, message)
    }

    /**
     * Shows message body on the RocketChatMessageLayout widget.
     */
    fun showBody(rocketChatMessageLayout: RocketChatMessageLayout) {
        rocketChatMessageLayout.setText(message.message)
    }

    /**
     * Shows message urls on the RocketChatMessageUrlsLayout widget.
     */
    fun showUrl(rocketChatMessageUrlsLayout: RocketChatMessageUrlsLayout) {
        val webContents = message.webContents
        if (webContents == null || webContents.isEmpty()) {
            rocketChatMessageUrlsLayout.visibility = View.GONE
        } else {
            rocketChatMessageUrlsLayout.setUrls(webContents, autoLoadImage)
            rocketChatMessageUrlsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Shows message attachments on the RocketChatMessageAttachmentsLayout widget.
     */
    fun showAttachment(rocketChatMessageAttachmentsLayout: RocketChatMessageAttachmentsLayout) {
        val attachments = message.attachments
        if (attachments == null || attachments.isEmpty()) {
            rocketChatMessageAttachmentsLayout.visibility = View.GONE
        } else {
            rocketChatMessageAttachmentsLayout.setAttachments(attachments, autoLoadImage)
            rocketChatMessageAttachmentsLayout.visibility = View.VISIBLE
        }
    }

    /**
     * Shows a error view in case of a failure to send or update (after a edition) a message.
     *
     * @param errorImageView The ImageView to show.
     */
    fun showErrorView(errorImageView: ImageView) {
        errorImageView.visibility = View.VISIBLE
    }

    /**
     * Shows a view that indicates a new day.
     *
     * @param dayLayout The layout of the view to show.
     * @param date The date to show.
     */
    fun showNewDay(dayLayout: View, date: TextView) {
        date.text = DateTime.fromEpocMs(message.timestamp, DateTime.Format.DATE)
        dayLayout.visibility = View.VISIBLE
    }

    /**
     * Hides views.
     *
     * @param views The views to hide.
     */
    fun hideViews(views: Array<View>) {
        views.forEach { view -> view.visibility = View.GONE }
    }
}