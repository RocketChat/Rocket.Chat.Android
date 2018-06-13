package chat.rocket.android.chatroom.viewmodel

import DateTimeHelper
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import chat.rocket.android.R
import chat.rocket.android.chatroom.domain.MessageReply
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.helper.MessageHelper
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.ChatRoomsInteractor
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.baseUrl
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.isNotNullNorEmpty
import chat.rocket.android.widget.emoji.EmojiParser
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType
import chat.rocket.core.model.Value
import chat.rocket.core.model.attachment.Attachment
import chat.rocket.core.model.attachment.AudioAttachment
import chat.rocket.core.model.attachment.AuthorAttachment
import chat.rocket.core.model.attachment.ColorAttachment
import chat.rocket.core.model.attachment.FileAttachment
import chat.rocket.core.model.attachment.GenericFileAttachment
import chat.rocket.core.model.attachment.ImageAttachment
import chat.rocket.core.model.attachment.MessageAttachment
import chat.rocket.core.model.attachment.VideoAttachment
import chat.rocket.core.model.isSystemMessage
import chat.rocket.core.model.url.Url
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import okhttp3.HttpUrl
import java.security.InvalidParameterException
import javax.inject.Inject

@PerFragment
class ViewModelMapper @Inject constructor(
    private val context: Context,
    private val parser: MessageParser,
    private val roomsInteractor: ChatRoomsInteractor,
    private val messageHelper: MessageHelper,
    tokenRepository: TokenRepository,
    serverInteractor: GetCurrentServerInteractor,
    getSettingsInteractor: GetSettingsInteractor,
    localRepository: LocalRepository
) {

    private val currentServer = serverInteractor.get()!!
    private val settings = getSettingsInteractor.get(currentServer)
    private val baseUrl = currentServer
    private val token = tokenRepository.get(currentServer)
    private val currentUsername: String? = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
    private val secondaryTextColor = ContextCompat.getColor(context, R.color.colorSecondaryText)

    suspend fun map(
        message: Message,
        roomViewModel: RoomViewModel = RoomViewModel(roles = emptyList(), isBroadcast = true)
    ): List<BaseViewModel<*>> {
        return translate(message, roomViewModel)
    }

    suspend fun map(
        messages: List<Message>,
        roomViewModel: RoomViewModel = RoomViewModel(roles = emptyList(), isBroadcast = true),
        asNotReversed: Boolean = false
    ): List<BaseViewModel<*>> =
        withContext(CommonPool) {
            val list = ArrayList<BaseViewModel<*>>(messages.size)

            messages.forEach {
                list.addAll(
                    if (asNotReversed) translateAsNotReversed(it, roomViewModel)
                    else translate(it, roomViewModel)
                )
            }
            return@withContext list
        }

    private suspend fun translate(
        message: Message,
        roomViewModel: RoomViewModel
    ): List<BaseViewModel<*>> =
        withContext(CommonPool) {
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

            if (isBroadcastReplyAvailable(roomViewModel, message)) {
                roomsInteractor.getById(currentServer, message.roomId)?.let { chatRoom ->
                    val replyViewModel = mapMessageReply(message, chatRoom)
                    list.first().nextDownStreamMessage = replyViewModel
                    list.add(0, replyViewModel)
                }
            }

            return@withContext list
        }

    private suspend fun translateAsNotReversed(
        message: Message,
        roomViewModel: RoomViewModel
    ): List<BaseViewModel<*>> =
        withContext(CommonPool) {
            val list = ArrayList<BaseViewModel<*>>()

            mapMessage(message).let {
                if (list.isNotEmpty()) {
                    it.preview = list.first().preview
                }
                list.add(it)
            }

            message.attachments?.forEach {
                val attachment = mapAttachment(message, it)
                attachment?.let {
                    list.add(attachment)
                }
            }

            message.urls?.forEach {
                val url = mapUrl(message, it)
                url?.let {
                    list.add(url)
                }
            }

            for (i in list.size - 1 downTo 0) {
                val next = if (i - 1 < 0) null else list[i - 1]
                list[i].nextDownStreamMessage = next
            }

            if (isBroadcastReplyAvailable(roomViewModel, message)) {
                roomsInteractor.getById(currentServer, message.roomId)?.let { chatRoom ->
                    val replyViewModel = mapMessageReply(message, chatRoom)
                    list.first().nextDownStreamMessage = replyViewModel
                    list.add(0, replyViewModel)
                }
            }

            list.dropLast(1).forEach {
                it.reactions = emptyList()
            }
            list.last().reactions = getReactions(message)
            list.last().nextDownStreamMessage = null

            return@withContext list
        }

    private fun isBroadcastReplyAvailable(roomViewModel: RoomViewModel, message: Message): Boolean {
        val senderUsername = message.sender?.username
        return roomViewModel.isRoom && roomViewModel.isBroadcast &&
                !message.isSystemMessage() &&
                senderUsername != currentUsername
    }

    private fun mapMessageReply(message: Message, chatRoom: ChatRoom): MessageReplyViewModel {
        val name = message.sender?.name
        val roomName =
            if (settings.useRealName() && name != null) name else message.sender?.username ?: ""
        val permalink = messageHelper.createPermalink(message, chatRoom)
        return MessageReplyViewModel(
            messageId = message.id,
            isTemporary = false,
            reactions = emptyList(),
            message = message,
            preview = mapMessagePreview(message),
            rawData = MessageReply(roomName = roomName, permalink = permalink),
            nextDownStreamMessage = null
        )
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

    private fun mapAttachment(message: Message, attachment: Attachment): BaseViewModel<*>? {
        return when (attachment) {
            is FileAttachment -> mapFileAttachment(message, attachment)
            is MessageAttachment -> mapMessageAttachment(message, attachment)
            is AuthorAttachment -> mapAuthorAttachment(message, attachment)
            is ColorAttachment -> mapColorAttachment(message, attachment)
            else -> null
        }
    }

    private fun mapColorAttachment(message: Message, attachment: ColorAttachment): BaseViewModel<*>? {
        return with(attachment) {
            val content = stripMessageQuotes(message)
            val id = attachmentId(message, attachment)

            ColorAttachmentViewModel(attachmentUrl = url, id = id, color = color.color,
                text = text, message = message, rawData = attachment,
                messageId = message.id, reactions = getReactions(message),
                preview = message.copy(message = content.message))
        }
    }

    private fun mapAuthorAttachment(message: Message, attachment: AuthorAttachment): AuthorAttachmentViewModel {
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

    private fun mapMessageAttachment(message: Message, attachment: MessageAttachment): MessageAttachmentViewModel {
        val attachmentAuthor = attachment.author
        val time = attachment.timestamp?.let { getTime(it) }
        val attachmentText = when (attachment.attachments.orEmpty().firstOrNull()) {
            is ImageAttachment -> context.getString(R.string.msg_preview_photo)
            is VideoAttachment -> context.getString(R.string.msg_preview_video)
            is AudioAttachment -> context.getString(R.string.msg_preview_audio)
            is GenericFileAttachment -> context.getString(R.string.msg_preview_file)
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
            is GenericFileAttachment -> GenericFileAttachmentViewModel(message, attachment,
                message.id, attachmentUrl, attachmentTitle, id, getReactions(message),
                preview = message.copy(message = context.getString(R.string.msg_preview_file)))
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

    private fun mapMessagePreview(message: Message): Message {
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

    private fun stripMessageQuotes(message: Message): Message {
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
                        color(secondaryTextColor) {
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

    private fun getContent(message: Message): CharSequence {
        return when (message.isSystemMessage()) {
            true -> getSystemMessage(message)
            false -> parser.render(message, currentUsername)
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
            is MessageType.UserMuted -> context.getString(R.string.message_muted, message.message, message.sender?.username)
            is MessageType.UserUnMuted -> context.getString(R.string.message_unmuted, message.message, message.sender?.username)
            is MessageType.SubscriptionRoleAdded -> context.getString(R.string.message_role_add, message.message, message.role, message.sender?.username)
            is MessageType.SubscriptionRoleRemoved -> context.getString(R.string.message_role_removed, message.message, message.role, message.sender?.username)
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