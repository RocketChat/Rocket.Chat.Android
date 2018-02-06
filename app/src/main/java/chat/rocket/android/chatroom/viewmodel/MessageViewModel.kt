package chat.rocket.android.chatroom.viewmodel

import DateTimeHelper
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import chat.rocket.android.R
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.helper.UrlHelper
import chat.rocket.android.server.domain.MessagesRepository
import chat.rocket.android.server.domain.SITE_URL
import chat.rocket.android.server.domain.useRealName
import chat.rocket.common.model.Token
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType.*
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.*
import chat.rocket.core.model.url.Url
import okhttp3.HttpUrl
import timber.log.Timber

data class MessageViewModel(val context: Context,
                            private val token: Token?,
                            private val message: Message,
                            private val settings: Map<String, Value<Any>>,
                            private val parser: MessageParser,
                            private val messagesRepository: MessagesRepository) {
    val id: String = message.id
    val roomId: String = message.roomId
    val time: CharSequence
    val sender: CharSequence
    val content: CharSequence
    var quote: Message? = null
    var urlsWithMeta = arrayListOf<Url>()
    var attachmentUrl: String? = null
    var attachmentTitle: CharSequence? = null
    var attachmentType: AttachmentType? = null
    var attachmentMessageText: String? = null
    var attachmentMessageAuthor: String? = null
    var attachmentMessageIcon: String? = null
    var attachmentTimestamp: Long? = null
    var systemMessage: Boolean = false

    init {
        sender = getSenderName()
        time = getTime(message.timestamp)

        val baseUrl = settings.get(SITE_URL)
        message.urls?.let {
            if (it.isEmpty()) return@let
            for (url in it) {
                if (url.meta != null) {
                    urlsWithMeta.add(url)
                }
                baseUrl?.let {
                    val quoteUrl = HttpUrl.parse(url.url)
                    val serverUrl = HttpUrl.parse(baseUrl.value.toString())
                    if (quoteUrl != null && serverUrl != null) {
                        makeQuote(quoteUrl, serverUrl)
                    }
                }
            }
        }

        message.attachments?.let {
            if (it.isEmpty() || it[0] == null) return@let

            if (it[0] is FileAttachment) {
                val attachment = it[0] as FileAttachment
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
            } else if (it[0] is MessageAttachment) {
                val attachment = it[0] as MessageAttachment
                attachmentType = AttachmentType.Message
                attachmentMessageText = attachment.text ?: ""
                attachmentMessageAuthor = attachment.author ?: ""
                attachmentMessageIcon = attachment.icon
                attachmentTimestamp = attachment.timestamp
            }
        }

        content = getContent(context)
    }

    private fun makeQuote(quoteUrl: HttpUrl, serverUrl: HttpUrl) {
        if (quoteUrl.host() == serverUrl.host()) {
            val msgIdToQuote = quoteUrl.queryParameter("msg")
            Timber.d("Will quote message Id: $msgIdToQuote")
            if (msgIdToQuote != null) {
                quote = messagesRepository.getById(msgIdToQuote)
            }
        }
    }

    fun getAvatarUrl(serverUrl: String): String? {
        return message.sender?.username.let {
            return@let UrlHelper.getAvatarUrl(serverUrl, it.toString())
        }
    }

    fun getOriginalMessage() = message.message

    private fun getTime(timestamp: Long) = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(timestamp))

    private fun getSenderName(): CharSequence {
        val username = message.sender?.username
        val realName = message.sender?.name
        val senderName = if (settings.useRealName()) realName else username
        return senderName ?: username.toString()
    }

    private fun getContent(context: Context): CharSequence {
        val contentMessage: CharSequence
        when (message.type) {
        //TODO: Add implementation for Welcome type.
            is MessageRemoved -> contentMessage = getSystemMessage(context.getString(R.string.message_removed))
            is UserJoined -> contentMessage = getSystemMessage(context.getString(R.string.message_user_joined_channel))
            is UserLeft -> contentMessage = getSystemMessage(context.getString(R.string.message_user_left))
            is UserAdded -> contentMessage = getSystemMessage(
                    context.getString(R.string.message_user_added_by, message.message, message.sender?.username))
            is RoomNameChanged -> contentMessage = getSystemMessage(
                    context.getString(R.string.message_room_name_changed, message.message, message.sender?.username))
            is UserRemoved -> contentMessage = getSystemMessage(
                    context.getString(R.string.message_user_removed_by, message.message, message.sender?.username))
            is MessagePinned -> contentMessage = getSystemMessage(
                    context.getString(R.string.message_pinned))
            else -> contentMessage = getNormalMessage()
        }
        return contentMessage
    }

    private fun getNormalMessage(): CharSequence {
        var quoteViewModel: MessageViewModel? = null
        if (quote != null) {
            val quoteMessage: Message = quote!!
            quoteViewModel = MessageViewModel(context, token, quoteMessage, settings, parser, messagesRepository)
        }
        return parser.renderMarkdown(message.message, quoteViewModel, urlsWithMeta)
    }

    private fun getSystemMessage(content: String): CharSequence {
        systemMessage = true
        val spannableMsg = SpannableStringBuilder(content)
        spannableMsg.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableMsg.length,
                0)
        spannableMsg.setSpan(ForegroundColorSpan(Color.GRAY), 0, spannableMsg.length,
                0)

        if (attachmentType == null) {
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
        } else if (attachmentType == AttachmentType.Message) {
            spannableMsg.append(quoteMessage(attachmentMessageAuthor!!, attachmentMessageText!!, attachmentTimestamp!!))
        }

        return spannableMsg
    }

    private fun quoteMessage(author: String, text: String, timestamp: Long): CharSequence {
        return SpannableStringBuilder().apply {
            val header = "\n$author ${getTime(timestamp)}\n"

            append(SpannableString(header).apply {
                setSpan(StyleSpan(Typeface.BOLD), 1, author.length + 1, 0)
                setSpan(MessageParser.QuoteMarginSpan(context.getDrawable(R.drawable.quote), 10), 1, length, 0)
                setSpan(AbsoluteSizeSpan(context.resources.getDimensionPixelSize(R.dimen.message_time_text_size)),
                        author.length + 1, length, 0)
            })
            append(SpannableString(parser.renderMarkdown(text)).apply {
                setSpan(MessageParser.QuoteMarginSpan(context.getDrawable(R.drawable.quote), 10), 0, length, 0)
            })
        }
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
    object Message : AttachmentType()
}