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
import chat.rocket.android.util.extension.isImage
import chat.rocket.android.util.extensions.avatarUrl
import chat.rocket.android.util.extensions.ifNotNullNorEmpty
import chat.rocket.android.util.extensions.isNotNullNorEmpty
import chat.rocket.common.model.roomTypeOf
import chat.rocket.core.model.ChatRoom
import chat.rocket.core.model.Message
import chat.rocket.core.model.MessageType
import chat.rocket.core.model.ReadReceipt
import chat.rocket.core.model.attachment.Attachment
import chat.rocket.core.model.attachment.Field
import chat.rocket.core.model.isSystemMessage
import chat.rocket.core.model.url.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        withContext(Dispatchers.IO) {
            return@withContext translate(message, roomUiModel)
        }

    suspend fun map(
        messages: List<Message>,
        roomUiModel: RoomUiModel = RoomUiModel(roles = emptyList(), isBroadcast = true),
        asNotReversed: Boolean = false
    ): List<BaseUiModel<*>> =
        withContext(Dispatchers.IO) {
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
    ): List<ReadReceiptViewModel> = withContext(Dispatchers.IO) {
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
        withContext(Dispatchers.IO) {
            val list = ArrayList<BaseUiModel<*>>()

            getChatRoomAsync(message.roomId)?.let { chatRoom ->
                message.urls?.forEach { url ->
                    if (url.url.isImage()) {
                        val attachment = Attachment(imageUrl = url.url)
                        mapAttachment(message, attachment, chatRoom)?.let { list.add(it) }
                    } else {
                        mapUrl(message, url, chatRoom)?.let { list.add(it) }
                    }
                }

                message.attachments?.mapNotNull { attachment ->
                    mapAttachment(message, attachment, chatRoom)
                }?.asReversed()?.let {
                    list.addAll(it)
                }

                mapMessage(message, chatRoom).let {
                    if (list.isNotEmpty()) {
                        it.preview = list.first().preview
                    }
                    list.add(it)
                }
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
    private suspend fun getChatRoomAsync(roomId: String): ChatRoom? = withContext(Dispatchers.IO) {
        return@withContext dbManager.getRoom(id = roomId)?.let {
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
            viewModel.menuItemsToHide.add(R.id.action_info)
        }
    }

    private suspend fun translateAsNotReversed(
        message: Message,
        roomUiModel: RoomUiModel
    ): List<BaseUiModel<*>> =
        withContext(Dispatchers.IO) {
            val list = ArrayList<BaseUiModel<*>>()

            getChatRoomAsync(message.roomId)?.let { chatRoom ->
                mapMessage(message, chatRoom).let {
                    if (list.isNotEmpty()) {
                        it.preview = list.first().preview
                    }
                    list.add(it)
                }

                message.attachments?.forEach {
                    val attachment = mapAttachment(message, it, chatRoom)
                    attachment?.let {
                        list.add(attachment)
                    }
                }

                message.urls?.forEach {
                    val url = mapUrl(message, it, chatRoom)
                    url?.let {
                        list.add(url)
                    }
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
            showDayMarker = false,
            permalink = messageHelper.createPermalink(message, chatRoom, false)
        )
    }

    private fun mapUrl(message: Message, url: Url, chatRoom: ChatRoom): BaseUiModel<*>? {
        if (url.ignoreParse || url.meta == null) return null

        val hostname = url.parsedUrl?.hostname ?: ""
        val thumb = url.meta?.imageUrl
        val title = url.meta?.title
        val description = url.meta?.description

        val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
        val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)
        val permalink = messageHelper.createPermalink(message, chatRoom, false)

        return UrlPreviewUiModel(message, url, message.id, title, hostname, description, thumb,
            getReactions(message), preview = message.copy(message = url.url), unread = message.unread,
            showDayMarker = false, currentDayMarkerText = dayMarkerText, permalink = permalink)
    }

    private fun mapAttachment(message: Message, attachment: Attachment, chatRoom: ChatRoom): BaseUiModel<*>? {
        return with(attachment) {
            val content = stripMessageQuotes(message)
            val id = attachmentId(message, attachment)

            val localDateTime = DateTimeHelper.getLocalDateTime(message.timestamp)
            val dayMarkerText = DateTimeHelper.getFormattedDateForMessages(localDateTime, context)
            val fieldsText = mapFields(fields)
            val permalink = messageHelper.createPermalink(message, chatRoom, false)

            val attachmentAuthor = attachment.authorName
            val time = attachment.timestamp?.let { getTime(it) }

            val imageUrl = attachmentUrl(attachment.imageUrl)
            val videoUrl = attachmentUrl(attachment.videoUrl)
            val audioUrl = attachmentUrl(attachment.audioUrl)
            val titleLink = attachmentUrl(attachment.titleLink)

            val attachmentTitle = attachmentTitle(attachment.title, imageUrl, videoUrl, audioUrl, titleLink)

            val attachmentText = attachmentText(attachment.text, attachment.attachments?.firstOrNull(), context)
            val attachmentDescription = attachmentDescription(attachment)

            AttachmentUiModel(
                message = message,
                rawData = this,
                messageId = message.id,
                reactions = getReactions(message),
                preview = message.copy(message = content.message),
                isTemporary = !message.synced,
                unread = message.unread,
                currentDayMarkerText = dayMarkerText,
                showDayMarker = false,
                permalink = permalink,
                id = id,
                title = attachmentTitle,
                description = attachmentDescription,
                authorName = attachmentAuthor,
                text = attachmentText,
                color = color?.color,
                imageUrl = imageUrl,
                videoUrl = videoUrl,
                audioUrl = audioUrl,
                titleLink = titleLink,
                type = type,
                messageLink = messageLink,
                timestamp = time,
                authorIcon = authorIcon,
                authorLink = authorLink,
                fields = fieldsText,
                buttonAlignment = buttonAlignment,
                actions = actions
            )
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

    private fun attachmentId(message: Message, attachment: Attachment): Long {
        return "${message.id}_${attachment.hashCode()}".hashCode().toLong()
    }

    private fun attachmentTitle(title: String?, vararg url: String?): CharSequence {
        title?.let { return it }

        url.filterNotNull().forEach {
            val fileUrl = HttpUrl.parse(it)
            fileUrl?.let { httpUrl ->
                return httpUrl.pathSegments().last()
            }
        }

        return ""
    }

    private fun attachmentUrl(url: String?): String? {
        if (url.isNullOrEmpty()) return null
        if (url!!.startsWith("http")) return url

        val fullUrl = "$baseUrl$url"
        val httpUrl = HttpUrl.parse(fullUrl)
        httpUrl?.let {
            return it.newBuilder().apply {
                addQueryParameter("rc_uid", token?.userId)
                addQueryParameter("rc_token", token?.authToken)
            }.build().toString()
        }

        // Fallback to baseUrl + url
        return fullUrl
    }

    private fun attachmentText(text: String?, attachment: Attachment?, context: Context): String? = attachment?.run {
        with(context) {
            when {
                imageUrl.isNotNullNorEmpty() -> getString(R.string.msg_preview_photo)
                videoUrl.isNotNullNorEmpty() -> getString(R.string.msg_preview_video)
                audioUrl.isNotNullNorEmpty() -> getString(R.string.msg_preview_audio)
                titleLink.isNotNullNorEmpty() &&
                        type?.contentEquals("file") == true ->
                    getString(R.string.msg_preview_file)
                else -> text
            }
        }
    } ?: text

    private fun attachmentDescription(attachment: Attachment): String? {
        return attachment.description
    }

    private suspend fun mapMessage(
        message: Message,
        chatRoom: ChatRoom
    ): MessageUiModel = withContext(Dispatchers.IO) {
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
        val permalink = messageHelper.createPermalink(message, chatRoom, false)

        val content = getContent(stripMessageQuotes(message))
        MessageUiModel(message = stripMessageQuotes(message), rawData = message,
            messageId = message.id, avatar = avatar!!, time = time, senderName = sender,
            content = content, isPinned = message.pinned, currentDayMarkerText = dayMarkerText,
            showDayMarker = false, reactions = getReactions(message), isFirstUnread = false,
            preview = preview, isTemporary = !synced, unread = unread, permalink = permalink,
            subscriptionId = chatRoom.subscriptionId)
    }

    private fun mapMessagePreview(message: Message): Message = when (message.isSystemMessage()) {
        false -> stripMessageQuotes(message)
        true -> message.copy(message = getSystemMessage(message).toString())
    }

    private fun getReactions(message: Message): List<ReactionUiModel> {
        val reactions = message.reactions?.let {
            val list = mutableListOf<ReactionUiModel>()
            val customEmojis = EmojiRepository.getCustomEmojis()
            it.getShortNames().forEach { shortname ->
                val usernames = it.getUsernames(shortname).orEmpty()
                val count = usernames.size
                val custom = customEmojis.firstOrNull { emoji -> emoji.shortname == shortname }
                list.add(
                    ReactionUiModel(messageId = message.id,
                        shortname = shortname,
                        unicode = EmojiParser.parse(context, shortname),
                        count = count,
                        usernames = usernames,
                        url = custom?.url,
                        isCustom = custom != null)
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
        message.senderAlias.ifNotNullNorEmpty { alias ->
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

    private fun getContent(message: Message): CharSequence = when (message.isSystemMessage()) {
        true -> getSystemMessage(message)
        false -> parser.render(message, currentUsername)
    }

    private fun getSystemMessage(message: Message): CharSequence {
        val content = with(context) {
            when (message.type) {
                //TODO: Add implementation for Welcome type.
                is MessageType.MessageRemoved -> getString(R.string.message_removed)
                is MessageType.UserJoined -> getString(R.string.message_user_joined_channel)
                is MessageType.UserLeft -> getString(R.string.message_user_left)
                is MessageType.UserAdded -> getString(R.string.message_user_added_by, message.message, message.sender?.username)
                is MessageType.RoomNameChanged -> getString(R.string.message_room_name_changed, message.message, message.sender?.username)
                is MessageType.UserRemoved -> getString(R.string.message_user_removed_by, message.message, message.sender?.username)
                is MessageType.MessagePinned -> getString(R.string.message_pinned)
                is MessageType.UserMuted -> getString(R.string.message_muted, message.message, message.sender?.username)
                is MessageType.UserUnMuted -> getString(R.string.message_unmuted, message.message, message.sender?.username)
                is MessageType.SubscriptionRoleAdded -> getString(R.string.message_role_add, message.message, message.role, message.sender?.username)
                is MessageType.SubscriptionRoleRemoved -> getString(R.string.message_role_removed, message.message, message.role, message.sender?.username)
                is MessageType.RoomChangedPrivacy -> getString(R.string.message_room_changed_privacy, message.message, message.sender?.username)
                is MessageType.JitsiCallStarted -> context.getString(
                    R.string.message_video_call_started, message.sender?.username
                )
                else -> throw InvalidParameterException("Invalid message type: ${message.type}")
            }
        }
        val spannableMsg = SpannableStringBuilder(content)
        spannableMsg.setSpan(StyleSpan(Typeface.ITALIC), 0, spannableMsg.length, 0)
        spannableMsg.setSpan(ForegroundColorSpan(Color.GRAY), 0, spannableMsg.length, 0)
        return spannableMsg
    }
}