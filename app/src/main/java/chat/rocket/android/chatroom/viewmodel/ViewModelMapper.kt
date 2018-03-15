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
import chat.rocket.android.widget.emoji.EmojiParser
import chat.rocket.core.TokenRepository
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.*
import chat.rocket.core.model.isSystemMessage
import chat.rocket.core.model.url.Url
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import okhttp3.HttpUrl
import timber.log.Timber
import java.security.InvalidParameterException
import javax.inject.Inject

class ViewModelMapper @Inject constructor(private val context: Context,
                                          private val parser: MessageParser,
                                          private val messagesRepository: MessagesRepository,
                                          tokenRepository: TokenRepository,
                                          localRepository: LocalRepository,
                                          serverInteractor: GetCurrentServerInteractor,
                                          getSettingsInteractor: GetSettingsInteractor) {

    private var settings: Map<String, Value<Any>> = getSettingsInteractor.get(serverInteractor.get()!!)!!
    private val baseUrl = settings.baseUrl()
    private val currentUsername: String? = localRepository.get(LocalRepository.USERNAME_KEY)
    private val token = tokenRepository.get()

    suspend fun map(message: Message): List<BaseViewModel<*>> {
        return translate(message)
    }

    suspend fun map(messages: List<Message>): List<BaseViewModel<*>> = withContext(CommonPool) {
        val list = ArrayList<BaseViewModel<*>>(messages.size)

        messages.forEach {
            list.addAll(translate(it))
        }

        return@withContext list
    }

    private suspend fun translate(message: Message): List<BaseViewModel<*>> = withContext(CommonPool) {
        val list = ArrayList<BaseViewModel<*>>()

        message.urls?.forEach {
            val url = mapUrl(message, it)
            url?.let { list.add(url) }
        }

        message.attachments?.forEach {
            val attachment = mapAttachment(message, it)
            attachment?.let { list.add(attachment) }
        }

        mapMessage(message).let {
            list.add(it)
        }

        return@withContext list
    }

    private fun mapUrl(message: Message, url: Url): BaseViewModel<*>? {
        if (url.ignoreParse || url.meta == null) return null

        val hostname = url.parsedUrl?.hostname ?: ""
        val thumb = url.meta?.imageUrl
        val title = url.meta?.title
        val description = url.meta?.description

        return UrlPreviewViewModel(message, url, message.id, title, hostname, description, thumb,
                getReactions(message))
    }

    private fun mapAttachment(message: Message, attachment: Attachment): BaseViewModel<*>? {
        return when (attachment) {
            is FileAttachment -> mapFileAttachment(message, attachment)
            else -> null
        }
    }

    private fun mapFileAttachment(message: Message, attachment: FileAttachment): BaseViewModel<*>? {
        val attachmentUrl = attachmentUrl(attachment)
        val attachmentTitle = attachmentTitle(attachment)
        val id = attachmentId(message, attachment)
        return when (attachment) {
            is ImageAttachment -> ImageAttachmentViewModel(message, attachment, message.id,
                    attachmentUrl, attachmentTitle, id, getReactions(message))
            is VideoAttachment -> VideoAttachmentViewModel(message, attachment, message.id,
                    attachmentUrl, attachmentTitle, id, getReactions(message))
            is AudioAttachment -> AudioAttachmentViewModel(message, attachment, message.id,
                    attachmentUrl, attachmentTitle, id, getReactions(message))
            else -> null
        }
    }

    private fun attachmentId(message: Message, attachment: FileAttachment): Long {
        return "${message.id}_${attachment.url}".hashCode().toLong()
    }

    private fun attachmentTitle(attachment: FileAttachment): CharSequence {
        return with(attachment) {
            title?.let { return@with it }

            val fileUrl = HttpUrl.parse(url)
            fileUrl?.let {
                return@with it.pathSegments().last()
            }

            return@with ""
        }
    }

    private fun attachmentUrl(attachment: FileAttachment): String {
        return with(attachment) {
            if (url.startsWith("http")) return@with url

            val fullUrl = "$baseUrl$url"
            val httpUrl = HttpUrl.parse(fullUrl)
            httpUrl?.let {
                return@with it.newBuilder().apply {
                    addQueryParameter("rc_uid", token?.userId)
                    addQueryParameter("rc_token", token?.authToken)
                }.build().toString()
            }

            // Fallback to baseUrl + url
            return@with fullUrl
        }
    }

    private suspend fun mapMessage(message: Message): MessageViewModel = withContext(CommonPool) {
        val sender = getSenderName(message)
        val time = getTime(message.timestamp)
        val avatar = getUserAvatar(message)

        val baseUrl = settings.baseUrl()
        var quote: Message? = null

        val urls = ArrayList<Url>()
        message.urls?.let {
            if (it.isEmpty()) return@let
            for (url in it) {
                urls.add(url)
                baseUrl?.let {
                    val quoteUrl = HttpUrl.parse(url.url)
                    val serverUrl = HttpUrl.parse(baseUrl)
                    if (quoteUrl != null && serverUrl != null) {
                        quote = makeQuote(quoteUrl, serverUrl)?.let {
                            getMessageWithoutQuoteMarkdown(it)
                        }
                    }
                }
            }
        }

        val content = getContent(context, getMessageWithoutQuoteMarkdown(message), quote)
        MessageViewModel(message = getMessageWithoutQuoteMarkdown(message), rawData = message,
                messageId = message.id, avatar = avatar!!, time = time, senderName = sender,
                content = content, isPinned = message.pinned, reactions = getReactions(message),
                isFirstUnread = false)
    }

    private fun getReactions(message: Message): List<ReactionViewModel> {
        val reactions = message.reactions?.let {
            val list = mutableListOf<ReactionViewModel>()
            it.getShortNames().forEach { shortname ->
                val usernames = it.getUsernames(shortname) ?: emptyList()
                val count = usernames.size
                list.add(
                        ReactionViewModel(messageId = message.id,
                                shortname = shortname,
                                unicode = EmojiParser.parse(shortname),
                                count = count,
                                usernames = usernames)
                )
            }
            list
        }
        return reactions ?: emptyList()
    }

    private fun getMessageWithoutQuoteMarkdown(message: Message): Message {
        val baseUrl = settings.baseUrl()
        return message.copy(
                message = message.message.replace("\\[\\s\\]\\($baseUrl.*\\)".toRegex(), "").trim()
        )
    }

    private fun getSenderName(message: Message): CharSequence {
        if (!message.senderAlias.isNullOrEmpty()) {
            return message.senderAlias!!
        }

        val username = message.sender?.username
        val realName = message.sender?.name
        val senderName = if (settings.useRealName()) realName else username
        return senderName ?: username.toString()
    }

    private fun getUserAvatar(message: Message): String? {
        message.avatar?.let {
            return it // Always give preference for overridden avatar from message
        }

        val username = message.sender?.username ?: "?"
        return baseUrl?.let {
            UrlHelper.getAvatarUrl(baseUrl, username)
        }
    }

    private fun getTime(timestamp: Long) = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(timestamp))

    private fun makeQuote(quoteUrl: HttpUrl, serverUrl: HttpUrl): Message? {
        if (quoteUrl.host() == serverUrl.host()) {
            val msgIdToQuote = quoteUrl.queryParameter("msg")
            Timber.d("Will quote message Id: $msgIdToQuote")
            return if (msgIdToQuote != null) messagesRepository.getById(msgIdToQuote) else null
        }
        return null
    }

    private suspend fun getContent(context: Context, message: Message, quote: Message?): CharSequence {
        return when (message.isSystemMessage()) {
            true -> getSystemMessage(message, context)
            false -> getNormalMessage(message, quote)
        }
    }

    private suspend fun getNormalMessage(message: Message, quote: Message?): CharSequence {
        var quoteViewModel: MessageViewModel? = null
        if (quote != null) {
            val quoteMessage: Message = quote
            quoteViewModel = mapMessage(quoteMessage)
        }
        return parser.renderMarkdown(message.message, quoteViewModel, currentUsername)
    }

    private fun getSystemMessage(message: Message, context: Context): CharSequence {
        val content = when (message.type) {
        //TODO: Add implementation for Welcome type.
            is MessageType.MessageRemoved -> context.getString(R.string.message_removed)
            is MessageType.UserJoined -> context.getString(R.string.message_user_joined_channel)
            is MessageType.UserLeft -> context.getString(R.string.message_user_left)
            is MessageType.UserAdded -> context.getString(R.string.message_user_added_by, message.message, message.sender?.username)
            is MessageType.RoomNameChanged -> context.getString(R.string.message_room_name_changed, message.message, message.sender?.username)
            is MessageType.UserRemoved -> context.getString(R.string.message_user_removed_by, message.message, message.sender?.username)
            is MessageType.MessagePinned -> {
                val attachment = message.attachments?.get(0)
                val pinnedSystemMessage = context.getString(R.string.message_pinned)
                if (attachment != null && attachment is MessageAttachment) {
                    return SpannableStringBuilder(pinnedSystemMessage)
                            .apply {
                                setSpan(StyleSpan(Typeface.ITALIC), 0, length, 0)
                                setSpan(ForegroundColorSpan(Color.GRAY), 0, length, 0)
                            }
                            .append(quoteMessage(attachment.author!!, attachment.text!!, attachment.timestamp!!))
                }
                return pinnedSystemMessage
            }
            else -> {
                throw InvalidParameterException("Invalid message type: ${message.type}")
            }
        }
        //isSystemMessage = true
        val spannableMsg = SpannableStringBuilder(content)
        spannableMsg.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableMsg.length,
                0)
        spannableMsg.setSpan(ForegroundColorSpan(Color.GRAY), 0, spannableMsg.length,
                0)

        /*if (attachmentType == null) {
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
        }*/

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
}