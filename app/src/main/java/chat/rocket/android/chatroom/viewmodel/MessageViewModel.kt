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
    val time: CharSequence
    val sender: CharSequence
    val content: CharSequence

    init {
        sender = getSenderName()
        content = getContent(RocketChatApplication.instance)
        time = getTime()
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
        return if (senderName == null) username.toString() else senderName.toString()
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

        if (message.type == USER_ADDED) {
            val userAddedStartIndex = 5
            val userAddedLastIndex = userAddedStartIndex + message.message.length
            val addedByStartIndex = userAddedLastIndex + 10
            val addedByLastIndex = addedByStartIndex + message.sender?.username!!.length
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), userAddedStartIndex, userAddedLastIndex,
                    0)
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), addedByStartIndex, addedByLastIndex,
                    0)
        } else if (message.type == ROOM_NAME_CHANGED) {
            val userAddedStartIndex = 22
            val userAddedLastIndex = userAddedStartIndex + message.message.length
            val addedByStartIndex = userAddedLastIndex + 4
            val addedByLastIndex = addedByStartIndex + message.sender?.username!!.length
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), userAddedStartIndex, userAddedLastIndex,
                    0)
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), addedByStartIndex, addedByLastIndex,
                    0)
        } else if (message.type == USER_REMOVED) {
            val userAddedStartIndex = 5
            val userAddedLastIndex = userAddedStartIndex + message.message.length
            val addedByStartIndex = userAddedLastIndex + 12
            val addedByLastIndex = addedByStartIndex + message.sender?.username!!.length
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), userAddedStartIndex, userAddedLastIndex,
                    0)
            spannableMsg.setSpan(StyleSpan(Typeface.BOLD_ITALIC), addedByStartIndex, addedByLastIndex,
                    0)
        }
        return spannableMsg
    }
}