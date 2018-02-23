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
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.common.model.Token
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType.*
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.*
import chat.rocket.core.model.url.Url
import okhttp3.HttpUrl

data class MessageViewModel(val context: Context,
                            private val token: Token?,
                            internal val message: Message,
                            private val settings: Map<String, Value<Any>>,
                            private val parser: MessageParser,
                            private val messagesRepository: MessagesRepository,
                            private val localRepository: LocalRepository,
                            private val currentServerRepository: CurrentServerRepository) {
    val id: String = message.id
    val avatarUri: String?
    val roomId: String = message.roomId
    val time: CharSequence
    val senderName: CharSequence
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
    var isSystemMessage: Boolean = false
    var isPinned: Boolean = false
    var currentUsername: String? = null
    private val baseUrl = settings.get(SITE_URL)

    init {
        currentUsername = localRepository.get(LocalRepository.USERNAME_KEY)
        avatarUri = getUserAvatar()
        time = getTime(message.timestamp)
        senderName = getSender()
        isPinned = message.pinned

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

        message.attachments?.let { attachments ->
            val attachment = attachments.firstOrNull()
            if (attachments.isEmpty() || attachment == null) return@let
            when (attachment) {
                is FileAttachment -> {
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
                is MessageAttachment -> {
                    attachmentType = AttachmentType.Message
                    attachmentMessageText = attachment.text ?: ""
                    attachmentMessageAuthor = attachment.author ?: ""
                    attachmentMessageIcon = attachment.icon
                    attachmentTimestamp = attachment.timestamp
                }
            }
        }
        content = getContent(context)
    }

    private fun getUserAvatar(): String? {
        val username = message.sender?.username ?: "?"
        return baseUrl?.let {
            UrlHelper.getAvatarUrl(baseUrl.value.toString(), username)
        }
    }

    private fun getTime(timestamp: Long) = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(timestamp))

    private fun getSender(): CharSequence {
        val useRealName = settings?.get(USE_REALNAME)?.value as Boolean
        val username = message.sender?.username
        val realName = message.sender?.name
        val senderName = if (useRealName) realName else username
        return senderName ?: context.getString(R.string.msg_unknown)
    }

        private fun makeQuote(quoteUrl: HttpUrl, serverUrl: HttpUrl) {
            if (quoteUrl.host() == serverUrl.host()) {
                val msgIdToQuote = quoteUrl.queryParameter("msg")
                if (msgIdToQuote != null) {
                    quote = messagesRepository.getById(msgIdToQuote)
                }
            }
        }

        /**
         * Get the original message as a String.
         */
        fun getOriginalMessage() = message.message

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
            quoteViewModel = MessageViewModel(context, token, quoteMessage, settings, parser,
                    messagesRepository, localRepository, currentServerRepository)
        }
        return parser.renderMarkdown(message.message, quoteViewModel, currentUsername)
    }

    private fun getSystemMessage(content: String): CharSequence {
        isSystemMessage = true
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
