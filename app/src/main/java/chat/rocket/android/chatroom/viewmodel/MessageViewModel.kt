package chat.rocket.android.chatroom.viewmodel

import DateTimeHelper
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import chat.rocket.android.R
import chat.rocket.android.app.RocketChatApplication
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.USE_REALNAME
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType.*
import chat.rocket.core.model.Value

data class MessageViewModel(private val message: Message,
                            private val settings: Map<String, Value<Any>>?) {
    val id: String = message.id
    val time: String
    val sender: String
    val content: SpannableString

    init {
        sender = getSenderName()
        content = getContent(RocketChatApplication.instance)
        time = getStringTime()
    }

    fun getAvatarUrl(serverUrl: String): String? {
        return message.sender?.username.let {
            return@let UrlHelper.getAvatarUrl(serverUrl, it.toString())
        }
    }

    private fun getStringTime() = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(message.timestamp))

    private fun getSenderName(): String {
        val useRealName = settings?.get(USE_REALNAME)?.value as Boolean
        val username = message.sender?.username
        val realName = message.sender?.name
        val senderName = if (useRealName) realName else username
        return senderName ?: username ?: "?"
    }

    private fun getContent(context: Context): SpannableString {
        return when (message.type) {
            //TODO: Add implementation for Welcome type.
            MESSAGE_REMOVED -> getSystemMessage(context.getString(R.string.message_removed))
            USER_JOINED -> getSystemMessage(context.getString(R.string.message_user_joined_channel))
            USER_LEFT -> getSystemMessage(context.getString(R.string.message_user_left))
            USER_ADDED -> getSystemMessage(context.getString(R.string.message_user_added_by, message.message, message.sender?.username))
            ROOM_NAME_CHANGED -> getSystemMessage(context.getString(R.string.message_room_name_changed, message.message, message.sender?.username))
            USER_REMOVED -> getSystemMessage(context.getString(R.string.message_user_removed_by, message.message, message.sender?.username))
            else -> getNormalMessage()
        }
    }

    private fun getNormalMessage(): SpannableString = SpannableString(message.message)

    private fun getSystemMessage(content: String): SpannableString {
        val spannableMsg = SpannableString(content)
        spannableMsg.setSpan(StyleSpan(Typeface.ITALIC),
                0,
                spannableMsg.length,
                0)
        spannableMsg.setSpan(ForegroundColorSpan(Color.GRAY),
                0,
                spannableMsg.length,
                0)

        val username = message.sender?.username
        val message = message.message

        val usernameTextStartIndex = if (username != null) content.indexOf(username) else -1
        val usernameTextEndIndex = if (username != null) usernameTextStartIndex + username.length else -1
        val messageTextStartIndex = if (message.isNotEmpty()) content.indexOf(message) else -1
        val messageTextEndIndex = messageTextStartIndex + message.length

        if (usernameTextStartIndex > -1) {
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC),
                    usernameTextStartIndex,
                    usernameTextEndIndex,
                    0)
        }

        if (messageTextStartIndex > -1) {
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC),
                    messageTextStartIndex,
                    messageTextEndIndex,
                    0)
        }

        return spannableMsg
    }
}