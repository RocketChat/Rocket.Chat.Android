package chat.rocket.android.chatroom.uimodel

import DateTimeHelper
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import androidx.core.content.ContextCompat
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.core.text.color
import androidx.core.text.scale
import chat.rocket.android.R
import chat.rocket.android.chatinformation.viewmodel.ReadReceiptViewModel
import chat.rocket.android.chatroom.domain.MessageReply
import chat.rocket.android.dagger.scope.PerFragment
import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.emoji.EmojiParser
import chat.rocket.android.emoji.EmojiRepository
import chat.rocket.android.helper.MessageHelper
import chat.rocket.android.helper.MessageParser
import chat.rocket.android.helper.UserHelper
import chat.rocket.android.infrastructure.LocalRepository
import chat.rocket.android.server.domain.GetCurrentServerInteractor
import chat.rocket.android.server.domain.GetSettingsInteractor
import chat.rocket.android.server.domain.TokenRepository
import chat.rocket.android.server.domain.baseUrl
import chat.rocket.android.server.domain.messageReadReceiptEnabled
import chat.rocket.android.server.domain.messageReadReceiptStoreUsers
import chat.rocket.android.server.domain.useRealName
import chat.rocket.android.server.infraestructure.ConnectionManagerFactory
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.isNotNullNorEmpty
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType
import chat.rocket.core.model.ReadReceipt
import chat.rocket.core.model.attachment.Attachment
import chat.rocket.core.model.attachment.AudioAttachment
import chat.rocket.core.model.attachment.AuthorAttachment
import chat.rocket.core.model.attachment.ColorAttachment
import chat.rocket.core.model.attachment.Field
import chat.rocket.core.model.attachment.FileAttachment
import chat.rocket.core.model.attachment.GenericFileAttachment
import chat.rocket.core.model.attachment.ImageAttachment
import chat.rocket.core.model.attachment.MessageAttachment
import chat.rocket.core.model.attachment.VideoAttachment
import chat.rocket.core.model.attachment.actions.ActionsAttachment
import chat.rocket.core.model.isSystemMessage
import chat.rocket.core.model.url.Url
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import okhttp3.HttpUrl
import java.security.InvalidParameterException
import java.util.*
import java.util.Collections.emptyList
import javax.inject.Inject

@PerFragment
class UiModelMapper @Inject constructor(
    private val context: Context,
    private val parser: MessageParser,
    private val dbManager: DatabaseManager,
    private val messageHelper: MessageHelper,
    private val userHelper: UserHelper,
    tokenRepository: TokenRepository,
    serverInteractor: GetCurrentServerInteractor,
    getSettingsInteractor: GetSettingsInteractor,
    localRepository: LocalRepository,
    factory: ConnectionManagerFactory
) {

    private val currentServer = serverInteractor.get()!!
    private val manager = factory.create(currentServer)
    private val client = manager.client
    private val settings = getSettingsInteractor.get(currentServer)
    private val baseUrl = currentServer
    private val token = tokenRepository.get(currentServer)
    private val currentUsername: String? = localRepository.get(LocalRepository.CURRENT_USERNAME_KEY)
    private val secondaryTextColor = ContextCompat.getColor(context, R.color.colorSecondaryText)

    suspend fun map(
        message: Message,
        roomUiModel: RoomUiModel = RoomUiModel(roles = emptyList(), isBroadcast = true)
    ): List<BaseUiModel<*>> =
        withContext(CommonPool) {
            return@withContext translate(message, roomUiModel)
        }

    suspend fun map(
        messages: List<Message>,
        roomUiModel: RoomUiModel = RoomUiModel(roles = emptyList(), isBroadcast = true),
        asNotReversed: Boolean = false
    ): List<BaseUiModel<*>> =
        withContext(CommonPool) {
            val list = ArrayList<BaseUiModel<*>>(messages.size)

            messages.forEach {
                list.addAll(
                    if (asNotReversed) translateAsNotReversed(it, roomUiModel)
                    else translate(it, roomUiModel)
                )
            }
            return@withContext list
        }

    suspend fun map(
        readReceipts: List<ReadReceipt>
    ): List<ReadReceiptViewModel> = withContext(CommonPool) {
        val list = arrayListOf<ReadReceiptViewModel>()

        readReceipts.forEach {
            list.add(
                ReadReceiptViewModel(
                    avatar = baseUrl.avatarUrl(it.user.username ?: ""),
                    name = userHelper.displayName(it.user),
                    time = DateTimeHelper.getTime(DateTimeHelper.getLocalDateTime(it.timestamp))
                )
            )
        }
        return@withContext list
    }

    private suspend fun translate(
        message: Message,
        roomUiModel: RoomUiModel
    ): List<BaseUiModel<*>> =
        withContext(CommonPool) {
            val list = ArrayList<BaseUiModel<*>>()

            message.urls?.forEach { url ->
                mapUrl(message, url)?.let { list.add(it) }
            }

            message.attachments?.mapNotNull { attachment ->
                mapAttachment(message, attachment)
            }?.asReversed()?.let {
                list.addAll(it)
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
                mapVisibleActions(list[i])
            }

            if (isBroadcastReplyAvailable(roomUiModel, message)) {
                getChatRoomAsync(message.roomId)?.let { chatRoom ->
                    val replyUiModel = mapMessageReply(message, chatRoom)
                    list.first().nextDownStreamMessage = replyUiModel
                    list.add(0, replyUiModel)
                }
            }

            return@withContext list
        }

    // TODO: move this to new interactor or FetchChatRoomsInteractor?
    private suspend fun getChatRoomAsync(roomId: String): ChatRoom? = withContext(CommonPool) {
        return@withContext dbManager.chatRoomDao().get(roomId)?.let {
            with(it.chatRoom) {
                ChatRoom(
                    id = id,
                    subscriptionId = subscriptionId,
                    type = roomTypeOf(type),
                    unread = unread,
                    broadcast = broadcast ?: false,
                    alert = alert,
                    fullName = fullname,
                    name = name,
                    favorite = favorite ?: false,
                    default = isDefault ?: false,
                    readonly = readonly,
                    open = open,
                    lastMessage = null,
                    archived = false,
                    status = null,
                    user = null,
                    userMentions = userMentions,
                    client = client,
                    announcement = null,
                    description = null,
                    groupMentions = groupMentions,
                    roles = null,
                    topic = null,
                    lastSeen = this.lastSeen,
                    timestamp = timestamp,
                    updatedAt = updatedAt
                )
            }
        }
    }

    private fun mapVisibleActions(viewModel: BaseUiModel<*>) {
        if (!settings.messageReadReceiptStoreUsers()) {
            viewModel.menuItemsToHide.add(R.id.action_message_info)
        }
    }

    private suspend fun translateAsNotReversed(
        message: Message,
        roomUiModel: RoomUiModel
    ): List<BaseUiModel<*>> =
        withContext(CommonPool) {
            val list = ArrayList<BaseUiModel<*>>()

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

            if (isBroadcastReplyAvailable(roomUiModel, message)) {
                getChatRoomAsync(message.roomId)?.let { chatRoom ->
                    val replyUiModel = mapMessageReply(message, chatRoom)
                    list.first().nextDownStreamMessage = replyUiModel
                    list.add(0, replyUiModel)
                }
            }

            list.dropLast(1).forEach {
                it.reactions = emptyList()
            }
            list.last().reactions = getReactions(message)
            list.last().nextDownStreamMessage = null

            return@withContext list
        }

    private fun isBroadcastReplyAvailable(roomUiModel: RoomUiModel, message: Message): Boolean {
        val senderUsername = message.sender?.username
        return roomUiModel.isRoom && roomUiModel.isBroadcast &&
            !message.isSystemMessage() &&
            senderUsername != currentUsername
    }

    private fun mapMessageReply(message: Message, chatRoom: ChatRoom): MessageReplyUiModel {
        val name = message.sender?.name
        val roomName =
            if (settings.useRealName() && name != null) name else message.sender?.username ?: ""
        val permalink = messageHelper.createPermalink(message, chatRoom)

        val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
        val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)

        return MessageReplyUiModel(
            messageId = message.id,
            isTemporary = false,
            reactions = emptyList(),
            message = message,
            preview = mapMessagePreview(message),
            rawData = MessageReply(roomName = roomName, permalink = permalink),
            nextDownStreamMessage = null,
            unread = message.unread,
            currentDayMarkerText = dayMarkerText,
            showDayMarker = false
        )
    }

    private fun mapUrl(message: Message, url: Url): BaseUiModel<*>? {
        if (url.ignoreParse || url.meta == null) return null

        val hostname = url.parsedUrl?.hostname ?: ""
        val thumb = url.meta?.imageUrl
        val title = url.meta?.title
        val description = url.meta?.description

        val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
        val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)

        return UrlPreviewUiModel(message, url, message.id, title, hostname, description, thumb,
            getReactions(message), preview = message.copy(message = url.url), unread = message.unread,
            showDayMarker = false, currentDayMarkerText = dayMarkerText)
    }

    private fun mapAttachment(message: Message, attachment: Attachment): BaseUiModel<*>? {
        return when (attachment) {
            is FileAttachment -> mapFileAttachment(message, attachment)
            is MessageAttachment -> mapMessageAttachment(message, attachment)
            is AuthorAttachment -> mapAuthorAttachment(message, attachment)
            is ColorAttachment -> mapColorAttachment(message, attachment)
            is ActionsAttachment -> mapActionsAttachment(message, attachment)
            else -> null
        }
    }

    private fun mapActionsAttachment(message: Message, attachment: ActionsAttachment): BaseUiModel<*>? {
        return with(attachment) {
            val content = stripMessageQuotes(message)

            val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
            val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)

            ActionsAttachmentUiModel(attachmentUrl = url, title = title,
                actions = actions, buttonAlignment = buttonAlignment, message = message, rawData = attachment,
                messageId = message.id, reactions = getReactions(message),
                preview = message.copy(message = content.message), unread = message.unread,
                showDayMarker = false, currentDayMarkerText = dayMarkerText)
        }
    }

    private fun mapColorAttachment(message: Message, attachment: ColorAttachment): BaseUiModel<*>? {
        return with(attachment) {
            val content = stripMessageQuotes(message)
            val id = attachmentId(message, attachment)

            val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
            val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)
            val fieldsText = mapFields(fields)

            ColorAttachmentUiModel(attachmentUrl = url, id = id, color = color.color,
                text = text, fields = fieldsText, message = message, rawData = attachment,
                messageId = message.id, reactions = getReactions(message),
                preview = message.copy(message = content.message), unread = message.unread,
                showDayMarker = false, currentDayMarkerText = dayMarkerText)
        }
    }

    private fun mapFields(fields: List<Field>?): CharSequence? {
        return fields?.let {
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
    }

    private fun mapAuthorAttachment(message: Message, attachment: AuthorAttachment): AuthorAttachmentUiModel {
        return with(attachment) {
            val content = stripMessageQuotes(message)

            val fieldsText = mapFields(fields)
            val id = attachmentId(message, attachment)

            val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
            val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)

            AuthorAttachmentUiModel(attachmentUrl = url, id = id, name = authorName,
                icon = authorIcon, fields = fieldsText, message = message, rawData = attachment,
                messageId = message.id, reactions = getReactions(message),
                preview = message.copy(message = content.message), unread = message.unread,
                showDayMarker = false, currentDayMarkerText = dayMarkerText)
        }
    }

    private fun mapMessageAttachment(message: Message, attachment: MessageAttachment): MessageAttachmentUiModel {
        val attachmentAuthor = attachment.author
        val time = attachment.timestamp?.let { getTime(it) }
        val attachmentText = when (attachment.attachments.orEmpty().firstOrNull()) {
            is ImageAttachment -> context.getString(R.string.msg_preview_photo)
            is VideoAttachment -> context.getString(R.string.msg_preview_video)
            is AudioAttachment -> context.getString(R.string.msg_preview_audio)
            is GenericFileAttachment -> context.getString(R.string.msg_preview_file)
            else -> attachment.text ?: ""
        }

        val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
        val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)

        val content = stripMessageQuotes(message)

        return MessageAttachmentUiModel(message = content, rawData = message,
            messageId = message.id, time = time, senderName = attachmentAuthor,
            content = attachmentText, isPinned = message.pinned, reactions = getReactions(message),
            preview = message.copy(message = content.message), unread = message.unread,
            currentDayMarkerText = dayMarkerText, showDayMarker = false)
    }

    private fun mapFileAttachment(message: Message, attachment: FileAttachment): BaseUiModel<*>? {
        val attachmentUrl = attachmentUrl(attachment)
        val attachmentTitle = attachmentTitle(attachment)
        val attachmentText = attachmentText(attachment)
        val attachmentDescription = attachmentDescription(attachment)
        val id = attachmentId(message, attachment)

        val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
        val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)

        return when (attachment) {
            is ImageAttachment -> ImageAttachmentUiModel(message, attachment, message.id,
                attachmentUrl, attachmentTitle, attachmentText, attachmentDescription, id, getReactions(message),
                preview = message.copy(message = context.getString(R.string.msg_preview_photo)), unread = message.unread,
                showDayMarker = false, currentDayMarkerText = dayMarkerText)
            is VideoAttachment -> VideoAttachmentUiModel(message, attachment, message.id,
                attachmentUrl, attachmentTitle, id, getReactions(message),
                preview = message.copy(message = context.getString(R.string.msg_preview_video)), unread = message.unread,
                showDayMarker = false, currentDayMarkerText = dayMarkerText)
            is AudioAttachment -> AudioAttachmentUiModel(message, attachment, message.id,
                attachmentUrl, attachmentTitle, id, getReactions(message),
                preview = message.copy(message = context.getString(R.string.msg_preview_audio)), unread = message.unread,
                showDayMarker = false, currentDayMarkerText = dayMarkerText)
            is GenericFileAttachment -> GenericFileAttachmentUiModel(message, attachment,
                message.id, attachmentUrl, attachmentTitle, id, getReactions(message),
                preview = message.copy(message = context.getString(R.string.msg_preview_file)), unread = message.unread,
                showDayMarker = false, currentDayMarkerText = dayMarkerText)
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

    private fun attachmentText(attachment: FileAttachment): String? {
        return attachment.text
    }

    private fun attachmentDescription(attachment: FileAttachment): String? {
        return attachment.description
    }

    private suspend fun mapMessage(message: Message): MessageUiModel = withContext(CommonPool) {
        val sender = getSenderName(message)
        val time = getTime(message.timestamp)
        val avatar = getUserAvatar(message)
        val preview = mapMessagePreview(message)
        val synced = message.synced
        val unread = if (settings.messageReadReceiptEnabled()) {
            message.unread ?: false
        } else {
            null
        }

        val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
        val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)

        val content = getContent(stripMessageQuotes(message))
        MessageUiModel(message = stripMessageQuotes(message), rawData = message,
            messageId = message.id, avatar = avatar!!, time = time, senderName = sender,
            content = content, isPinned = message.pinned, currentDayMarkerText = dayMarkerText,
            showDayMarker = false, reactions = getReactions(message), isFirstUnread = false,
            preview = preview, isTemporary = !synced, unread = unread)
    }

    private fun mapMessagePreview(message: Message): Message {
        return when (message.isSystemMessage()) {
            false -> stripMessageQuotes(message)
            true -> message.copy(message = getSystemMessage(message).toString())
        }
    }

    private fun getReactions(message: Message): List<ReactionUiModel> {
        val reactions = message.reactions?.let {
            val list = mutableListOf<ReactionUiModel>()
            val customEmojis = EmojiRepository.getCustomEmojis()
            it.getShortNames().forEach { shortname ->
                val usernames = it.getUsernames(shortname) ?: emptyList()
                val count = usernames.size
                val custom = customEmojis.firstOrNull { emoji -> emoji.shortname == shortname }
                list.add(
                    ReactionUiModel(messageId = message.id,
                        shortname = shortname,
                        unicode = EmojiParser.parse(context, shortname),
                        count = count,
                        usernames = usernames,
                        url = custom?.url)
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
        return baseUrl.let {
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
            is MessageType.RoomChangedPrivacy -> context.getString(R.string.message_room_changed_privacy, message.message, message.sender?.username)
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