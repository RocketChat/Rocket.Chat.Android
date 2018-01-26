package chat.rocket.android.chatroom.viewmodel

import DateTimeHelper
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import chat.rocket.android.R
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.SITE_URL
import chat.rocket.android.server.domain.USE_REALNAME
import chat.rocket.common.model.Token
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType.*
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.AudioAttachment
import chat.rocket.core.model.attachment.FileAttachment
import chat.rocket.core.model.attachment.ImageAttachment
import chat.rocket.core.model.attachment.VideoAttachment
import okhttp3.HttpUrl

data class MessageViewModel(val context: Context,
                            private val token: Token?,
                            private val message: Message,
                            private val settings: Map<String, Value<Any>>?) {
    val id: String = message.id
    val time: CharSequence
    val sender: CharSequence
    val content: CharSequence
    var attachmentUrl: String? = null
    var attachmentTitle: CharSequence? = null
    var attachmentType: AttachmentType? = null

    init {
        sender = getSenderName()
        content = getContent(context)
        time = getTime()

        message.attachments?.let {
            if (it.isEmpty() || it[0] == null) return@let
            val attachment = it[0] as FileAttachment
            val baseUrl = settings?.get(SITE_URL)
            baseUrl?.let {
                attachmentUrl = attachmentUrl("${baseUrl.value}${attachment.url}")
                attachmentTitle = attachment.title

                attachmentType = when (attachment) {
                    is ImageAttachment -> AttachmentType.Image
                    is VideoAttachment -> AttachmentType.Video
                    is AudioAttachment -> AttachmentType.Audio
                    else -> null
                }
            }
        }
    }

    fun getAvatarUrl(serverUrl: String): String? {
        return message.sender?.username.let {
            return@let UrlHelper.getAvatarUrl(serverUrl, it.toString())
        }
    }

    fun getTime() = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(message.timestamp))

    fun getSenderName(): CharSequence {
        val useRealName = settings?.get(USE_REALNAME)?.value as Boolean
        val username = message.sender?.username
        val realName = message.sender?.name
        val senderName = if (useRealName) realName else username
        return senderName ?: username.toString()
    }

    fun getContent(context: Context): CharSequence {
        val contentMessage: CharSequence
        when (message.type) {
        //TODO: Add implementation for Welcome type.
            MESSAGE_REMOVED -> contentMessage = getSystemMessage(context.getString(R.string.message_removed))
            USER_JOINED -> contentMessage = getSystemMessage(context.getString(R.string.message_user_joined_channel))
            USER_LEFT -> contentMessage = getSystemMessage(context.getString(R.string.message_user_left))
            USER_ADDED -> contentMessage = getSystemMessage(
                    context.getString(R.string.message_user_added_by, message.message, message.sender?.username))
            ROOM_NAME_CHANGED -> contentMessage = getSystemMessage(
                    context.getString(R.string.message_room_name_changed, message.message, message.sender?.username))
            USER_REMOVED -> contentMessage = getSystemMessage(
                    context.getString(R.string.message_user_removed_by, message.message, message.sender?.username))
            else -> contentMessage = getNormalMessage()
        }
        return contentMessage
    }

    private fun getNormalMessage() = message.message

    private fun getSystemMessage(content: String): CharSequence {
        val spannableMsg = SpannableString(content)
        spannableMsg.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableMsg.length,
                0)
        spannableMsg.setSpan(ForegroundColorSpan(Color.GRAY), 0, spannableMsg.length,
                0)

        val username = message.sender?.username
        val message = message.message

        val usernameTextStartIndex = if (username != null) content.indexOf(username) else -1
        val usernameTextEndIndex = if (username != null) usernameTextStartIndex + username.length else -1
        val messageTextStartIndex = if (message.isNotEmpty()) content.indexOf(message) else -1
        val messageTextEndIndex = messageTextStartIndex + message.length

        if (usernameTextStartIndex > -1) {
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), usernameTextStartIndex, usernameTextEndIndex,
                    0)
        }

        if (messageTextStartIndex > -1) {
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), messageTextStartIndex, messageTextEndIndex,
                    0)
        }

        return spannableMsg
    }

    private fun attachmentUrl(url: String): String {
        var response = url
        val httpUrl = HttpUrl.parse(url)
        httpUrl?.let {
            response = it.newBuilder().apply {
                addQueryParameter("rc_uid", token?.userId)
                addQueryParameter("rc_token", token?.authToken)
            }.build().toString()
        }

        return response
    }
}

sealed class AttachmentType {
    object Image : AttachmentType()
    object Video : AttachmentType()
    object Audio : AttachmentType()
}