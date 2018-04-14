package chat.rocket.android.chatroom.viewmodel

import DateTimeHelper
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.text.Html
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import chat.rocket.android.R
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.*
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.isNotNullNorEmpty
import chat.rocket.android.widget.emoji.EmojiParser
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.*
import chat.rocket.core.model.isSystemMessage
import chat.rocket.core.model.url.Url
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import okhttp3.HttpUrl
import java.security.InvalidParameterException
import javax.inject.Inject

class ViewModelMapper @Inject constructor(private val context: Context,
                                          private val parser: MessageParser,
                                          private val messagesRepository: MessagesRepository,
                                          private val getAccountInteractor: GetAccountInteractor,
                                          tokenRepository: TokenRepository,
                                          serverInteractor: GetCurrentServerInteractor,
                                          getSettingsInteractor: GetSettingsInteractor,
                                          localRepository: LocalRepository) {

    private val currentServer = serverInteractor.get()!!
    private val settings: Map<String, Value<Any>> = getSettingsInteractor.get(currentServer)
    private val baseUrl = settings.baseUrl()
    private val token = tokenRepository.get(currentServer)
    private val currentUsername: String? = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
    private val secundaryTextColor = ContextCompat.getColor(context, R.color.colorSecondaryText)

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
            if (list.isNotEmpty()) {
                it.preview = list.first().preview
            }
            list.add(it)
        }

        for (i in list.size - 1 downTo 0) {
            val next = if (i - 1 < 0) null else list[i - 1]
            list[i].nextDownStreamMessage = next
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
                getReactions(message), preview = message.copy(message = url.url))
    }

    private suspend fun mapAttachment(message: Message, attachment: Attachment): BaseViewModel<*>? {
        return when (attachment) {
            is FileAttachment -> mapFileAttachment(message, attachment)
            is MessageAttachment -> mapMessageAttachment(message, attachment)
            is AuthorAttachment -> mapAuthorAttachment(message, attachment)
            is ColorAttachment -> mapColorAttachment(message, attachment)
            else -> null
        }
    }

    private suspend fun mapColorAttachment(message: Message, attachment: ColorAttachment): BaseViewModel<*>? {
        return with(attachment) {
            val content = stripMessageQuotes(message)
            val id = attachmentId(message, attachment)

            ColorAttachmentViewModel(attachmentUrl = url, id = id, color = color.color,
                    text = text, message = message, rawData = attachment,
                    messageId = message.id, reactions = getReactions(message),
                    preview = message.copy(message = content.message))
        }
    }

    private suspend fun mapAuthorAttachment(message: Message, attachment: AuthorAttachment): AuthorAttachmentViewModel {
        return with(attachment) {
            val content = stripMessageQuotes(message)

            val fieldsText = fields?.let {
                buildSpannedString {
                    it.forEachIndexed { index, field ->
                        bold { append(field.title) }
                        append("\n")
                        if (field.value.isNotEmpty()) {
                            append(field.value)
                        }

                        if (index != it.size - 1) { // it is not the last one, append a new line
                            append("\n\n")
                        }
                    }
                }
            }
            val id = attachmentId(message, attachment)

            AuthorAttachmentViewModel(attachmentUrl = url, id = id, name = authorName,
                    icon = authorIcon, fields = fieldsText, message = message, rawData = attachment,
                    messageId = message.id, reactions = getReactions(message),
                    preview = message.copy(message = content.message))
        }
    }

    private suspend fun mapMessageAttachment(message: Message, attachment: MessageAttachment): MessageAttachmentViewModel {
        val attachmentAuthor = attachment.author
        val time = attachment.timestamp?.let { getTime(it) }
        val attachmentText = when (attachment.attachments.orEmpty().firstOrNull()) {
            is ImageAttachment -> context.getString(R.string.msg_preview_photo)
            is VideoAttachment -> context.getString(R.string.msg_preview_video)
            is AudioAttachment -> context.getString(R.string.msg_preview_audio)
            else -> attachment.text ?: ""
        }
        val content = stripMessageQuotes(message)
        return MessageAttachmentViewModel(message = content, rawData = message,
                messageId = message.id, time = time, senderName = attachmentAuthor,
                content = attachmentText, isPinned = message.pinned, reactions = getReactions(message),
                preview = message.copy(message = content.message))
    }

    private fun mapFileAttachment(message: Message, attachment: FileAttachment): BaseViewModel<*>? {
        val attachmentUrl = attachmentUrl(attachment)
        val attachmentTitle = attachmentTitle(attachment)
        val id = attachmentId(message, attachment)
        return when (attachment) {
            is ImageAttachment -> ImageAttachmentViewModel(message, attachment, message.id,
                    attachmentUrl, attachmentTitle, id, getReactions(message),
                    preview = message.copy(message = context.getString(R.string.msg_preview_photo)))
            is VideoAttachment -> VideoAttachmentViewModel(message, attachment, message.id,
                    attachmentUrl, attachmentTitle, id, getReactions(message),
                    preview = message.copy(message = context.getString(R.string.msg_preview_video)))
            is AudioAttachment -> AudioAttachmentViewModel(message, attachment, message.id,
                    attachmentUrl, attachmentTitle, id, getReactions(message),
                    preview = message.copy(message = context.getString(R.string.msg_preview_audio)))
            else -> null
        }
    }

    private fun attachmentId(message: Message, attachment: Attachment): Long {
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
        val preview = mapMessagePreview(message)
        val isTemp = message.isTemporary ?: false

        val content = getContent(stripMessageQuotes(message))
        MessageViewModel(message = stripMessageQuotes(message), rawData = message,
                messageId = message.id, avatar = avatar!!, time = time, senderName = sender,
                content = content, isPinned = message.pinned, reactions = getReactions(message),
                isFirstUnread = false, preview = preview, isTemporary = isTemp)
    }

    private suspend fun mapMessagePreview(message: Message): Message {
        return when (message.isSystemMessage()) {
            false -> stripMessageQuotes(message)
            true -> message.copy(message = getSystemMessage(message).toString())
        }
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

    private suspend fun stripMessageQuotes(message: Message): Message {
        val baseUrl = settings.baseUrl()
        return message.copy(
                message = message.message.replace("\\[[^\\]]+\\]\\($baseUrl[^)]+\\)".toRegex(), "").trim()
        )
    }

    private fun getSenderName(message: Message): CharSequence {
        val username = message.sender?.username
        message.senderAlias.isNotNullNorEmpty { alias ->
            return buildSpannedString {
                append(alias)
                username?.let {
                    append(" ")
                    scale(0.8f) {
                        color(secundaryTextColor) {
                            append("@$username")
                        }
                    }
                }
            }
        }

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
            baseUrl.avatarUrl(username)
        }
    }

    private fun getTime(timestamp: Long) = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(timestamp))

    private suspend fun getContent(message: Message): CharSequence {
        return when (message.isSystemMessage()) {
            true -> getSystemMessage(message)
            false -> parser.renderMarkdown(message, currentUsername)
        }
    }

    private fun getSystemMessage(message: Message): CharSequence {
        val content = when (message.type) {
        //TODO: Add implementation for Welcome type.
            is MessageType.MessageRemoved -> context.getString(R.string.message_removed)
            is MessageType.UserJoined -> context.getString(R.string.message_user_joined_channel)
            is MessageType.UserLeft -> context.getString(R.string.message_user_left)
            is MessageType.UserAdded -> context.getString(R.string.message_user_added_by, message.message, message.sender?.username)
            is MessageType.RoomNameChanged -> context.getString(R.string.message_room_name_changed, message.message, message.sender?.username)
            is MessageType.UserRemoved -> context.getString(R.string.message_user_removed_by, message.message, message.sender?.username)
            is MessageType.MessagePinned -> context.getString(R.string.message_pinned)
            else -> {
                throw InvalidParameterException("Invalid message type: ${message.type}")
            }
        }
        val spannableMsg = SpannableStringBuilder(content)
        spannableMsg.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableMsg.length,
                0)
        spannableMsg.setSpan(ForegroundColorSpan(Color.GRAY), 0, spannableMsg.length,
                0)

        return spannableMsg
    }
}