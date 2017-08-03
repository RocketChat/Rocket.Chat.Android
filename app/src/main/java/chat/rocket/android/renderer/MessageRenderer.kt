package chat.rocket.android.renderer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import chat.rocket.android.R
import chat.rocket.android.helper.DateTime
import chat.rocket.android.widget.AbsoluteUrl
import chat.rocket.android.widget.RocketChatAvatar
import chat.rocket.android.widget.helper.UserAvatarHelper
import chat.rocket.android.widget.message.RocketChatMessageAttachmentsLayout
import chat.rocket.android.widget.message.RocketChatMessageLayout
import chat.rocket.android.widget.message.RocketChatMessageUrlsLayout
import chat.rocket.core.SyncState
import chat.rocket.core.models.Message

class MessageRenderer(val message: Message, val autoLoadImage: Boolean) {

    /**
     * Show user's avatar image in RocketChatAvatar widget.
     */
    fun showAvatar(rocketChatAvatarWidget: RocketChatAvatar, hostname: String, userNotFoundAvatarImageView: ImageView) {
        val username: String? = message.user?.username
        if (username != null) {
            userNotFoundAvatarImageView.visibility = View.GONE
            val placeholderDrawable = UserAvatarHelper.getTextDrawable(username, rocketChatAvatarWidget.context)
            if (message.avatar != null) {
                // Load user's avatar image from Oauth provider URI.
                rocketChatAvatarWidget.loadImage(message.avatar, placeholderDrawable)
            } else {
                rocketChatAvatarWidget.loadImage(UserAvatarHelper.getUri(hostname, username), placeholderDrawable)
            }
        } else {
            rocketChatAvatarWidget.visibility = View.GONE
            userNotFoundAvatarImageView.visibility = View.VISIBLE
        }
    }

    /**
     * Show username in textView.
     */
    fun showUsername(usernameTextView: TextView, subUsernameTextView: TextView?) {
        if (message.alias == null) {
            usernameTextView.text = message.user?.username ?: usernameTextView.context.getText(R.string.user_not_found)
        } else {
            usernameTextView.text = message.alias
            val username: String? = message.user?.username
            if (username != null && subUsernameTextView != null) {
                subUsernameTextView.text = subUsernameTextView.context.getString(R.string.sub_username, username)
                subUsernameTextView.visibility = View.VISIBLE
            }
        }
    }

    /**
     * Show timestamp or message state in textView.
     */
    fun showTimestampOrMessageState(textView: TextView) {
        when (message.syncState) {
            SyncState.SYNCING -> textView.text = textView.context.getText(R.string.sending)
            SyncState.NOT_SYNCED -> textView.text = textView.context.getText(R.string.not_synced)
            SyncState.FAILED -> textView.text = textView.context.getText(R.string.failed_to_sync)
            else -> textView.text = DateTime.fromEpocMs(message.timestamp, DateTime.Format.TIME)
        }
    }

    /**
     * Show body in RocketChatMessageLayout widget.
     */
    fun showBody(rocketChatMessageLayout: RocketChatMessageLayout) {
        rocketChatMessageLayout.setText(message.message)
    }

    /**
     * Show urls in RocketChatMessageUrlsLayout widget.
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
     * show attachments in RocketChatMessageAttachmentsLayout widget.
     */
    fun showAttachment(rocketChatMessageAttachmentsLayout: RocketChatMessageAttachmentsLayout, absoluteUrl: AbsoluteUrl?) {
        val attachments = message.attachments
        if (attachments == null || attachments.isEmpty()) {
            rocketChatMessageAttachmentsLayout.visibility = View.GONE
        } else {
            rocketChatMessageAttachmentsLayout.setAbsoluteUrl(absoluteUrl)
            rocketChatMessageAttachmentsLayout.setAttachments(attachments, autoLoadImage)
            rocketChatMessageAttachmentsLayout.visibility = View.VISIBLE
        }
    }
}