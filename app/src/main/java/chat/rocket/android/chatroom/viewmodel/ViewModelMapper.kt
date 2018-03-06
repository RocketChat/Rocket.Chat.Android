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
import chat.rocket.core.TokenRepository
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.*
import chat.rocket.core.model.url.Url
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import okhttp3.HttpUrl
import timber.log.Timber
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

    private suspend fun translate(message: Message): List<BaseViewModel<*>>  = withContext(CommonPool) {
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

        return UrlPreviewViewModel(url, message.id, title, hostname, description, thumb)
    }

    private fun mapAttachment(message: Message, attachment: Attachment): BaseViewModel<*>? {
        return when (attachment) {
            is FileAttachment -> mapFileAttachment(message, attachment)
            else -> null
        }
    }

    private fun mapFileAttachment(message: Message, attachment: FileAttachment): BaseViewModel<*>? {
        val attachmentUrl = attachmentUrl("$baseUrl${attachment.url}")
        val attachmentTitle = attachment.title
        val id = "${message.id}_${attachment.titleLink}".hashCode().toLong()
        return when (attachment) {
            is ImageAttachment -> ImageAttachmentViewModel(attachment, message.id, attachmentUrl,
                    attachmentTitle ?: "", id)
            is VideoAttachment -> VideoAttachmentViewModel(attachment, message.id,
                    attachmentUrl, attachmentTitle ?: "", id)
            is AudioAttachment -> AudioAttachmentViewModel(attachment,
                    message.id, attachmentUrl, attachmentTitle ?: "", id)
            else -> null
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
                        quote = makeQuote(quoteUrl, serverUrl)
                    }
                }
            }
        }

        val content = getContent(context, message, quote)
        MessageViewModel(rawData = message, messageId = message.id,
                avatar = avatar!!, time = time, senderName = sender,
                content = content.first, isPinned = message.pinned,
                isSystemMessage = content.second)
    }

    private fun getSenderName(message: Message): CharSequence {
        val username = message.sender?.username
        val realName = message.sender?.name
        val senderName = if (settings.useRealName()) realName else username
        return senderName ?: username.toString()
    }

    private fun getUserAvatar(message: Message): String? {
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

    private suspend fun getContent(context: Context, message: Message, quote: Message?): Pair<CharSequence, Boolean> {
        var systemMessage = true
        val content = when (message.type) {
        //TODO: Add implementation for Welcome type.
            is MessageType.MessageRemoved -> getSystemMessage(context.getString(R.string.message_removed))
            is MessageType.UserJoined -> getSystemMessage(context.getString(R.string.message_user_joined_channel))
            is MessageType.UserLeft -> getSystemMessage(context.getString(R.string.message_user_left))
            is MessageType.UserAdded -> getSystemMessage(context.getString(R.string.message_user_added_by, message.message, message.sender?.username))
            is MessageType.RoomNameChanged -> getSystemMessage(context.getString(R.string.message_room_name_changed, message.message, message.sender?.username))
            is MessageType.UserRemoved -> getSystemMessage(context.getString(R.string.message_user_removed_by, message.message, message.sender?.username))
            is MessageType.MessagePinned -> getSystemMessage(context.getString(R.string.message_pinned))
            else -> {
                systemMessage = false
                getNormalMessage(message, quote)
            }
        }

        return Pair(content, systemMessage)
    }

    private suspend fun getNormalMessage(message: Message, quote: Message?): CharSequence {
        var quoteViewModel: MessageViewModel? = null
        if (quote != null) {
            val quoteMessage: Message = quote
            quoteViewModel = map(quoteMessage).first { it is MessageViewModel } as MessageViewModel
        }
        return parser.renderMarkdown(message.message, quoteViewModel, currentUsername)
    }

    private fun getSystemMessage(content: String): CharSequence {
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