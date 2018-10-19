package chat.rocket.android.server.infraestructure

import chat.rocket.android.db.DatabaseManager
import chat.rocket.android.db.model.AttachmentActionEntity
import chat.rocket.android.db.model.AttachmentEntity
import chat.rocket.android.db.model.FullMessage
import chat.rocket.android.db.model.ReactionEntity
import chat.rocket.android.db.model.UrlEntity
import chat.rocket.android.db.model.UserEntity
import chat.rocket.common.model.SimpleRoom
import chat.rocket.common.model.SimpleUser
import chat.rocket.core.model.Message
import chat.rocket.core.model.Reactions
import chat.rocket.core.model.attachment.Attachment
import chat.rocket.core.model.attachment.AudioAttachment
import chat.rocket.core.model.attachment.AuthorAttachment
import chat.rocket.core.model.attachment.Color
import chat.rocket.core.model.attachment.ColorAttachment
import chat.rocket.core.model.attachment.DEFAULT_COLOR_STR
import chat.rocket.core.model.attachment.Field
import chat.rocket.core.model.attachment.GenericFileAttachment
import chat.rocket.core.model.attachment.ImageAttachment
import chat.rocket.core.model.attachment.MessageAttachment
import chat.rocket.core.model.attachment.VideoAttachment
import chat.rocket.core.model.attachment.actions.Action
import chat.rocket.core.model.attachment.actions.ActionsAttachment
import chat.rocket.core.model.attachment.actions.ButtonAction
import chat.rocket.core.model.messageTypeOf
import chat.rocket.core.model.url.Meta
import chat.rocket.core.model.url.ParsedUrl
import chat.rocket.core.model.url.Url
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext

class DatabaseMessageMapper(private val dbManager: DatabaseManager) {
    suspend fun map(message: FullMessage): Message? = map(listOf(message)).firstOrNull()

    suspend fun map(messages: List<FullMessage>): List<Message> {
        val list = mutableListOf<Message>()
        messages.forEach { message ->
            val favorites = mutableListOf<SimpleUser>()
            message.favorites.forEach { user ->
                favorites.add(mapUser(user))
            }

            val mentions = mutableListOf<SimpleUser>()
            message.mentions.forEach { user ->
                mentions.add(mapUser(user))
            }

            val channels = mutableListOf<SimpleRoom>()
            message.message.channels?.forEach { channel ->
                channels.add(SimpleRoom(channel.roomId, channel.roomName))
            }

            with(message.message) {
                val sender = this.message.senderId?.let { id ->
                    SimpleUser(id, this.senderUsername, this.senderName)
                }
                val editedBy = this.message.editedBy?.let { id ->
                    SimpleUser(id, this.editUsername, this.editName)
                }
                val urls = this.urls?.let { mapUrl(it) }
                val reactions = this.reactions?.let { mapReactions(it) }
                val attachments = this.attachments?.let { mapAttachments(it).asReversed() }
                val messageType = messageTypeOf(this.message.type)

                list.add(Message(
                        id = this.message.id,
                        roomId = this.message.roomId,
                        message = this.message.message,
                        timestamp = this.message.timestamp,
                        sender = sender,
                        updatedAt = this.message.updatedAt,
                        editedAt = this.message.editedAt,
                        editedBy = editedBy,
                        senderAlias = this.message.senderAlias,
                        avatar = this.message.avatar,
                        type = messageType,
                        groupable = this.message.groupable,
                        parseUrls = this.message.parseUrls,
                        urls = urls,
                        mentions = mentions,
                        channels = channels,
                        attachments = attachments,
                        pinned = this.message.pinned,
                        starred = favorites,
                        reactions = reactions,
                        role = this.message.role,
                        synced = this.message.synced,
                        unread = this.message.unread
                ))
            }
        }

        return list
    }

    private fun mapReactions(reactions: List<ReactionEntity>): Reactions {
        val map = Reactions()
        reactions.forEach { reaction ->
            val usernames = reaction.usernames.split(",").map { it.trim() }
            map[reaction.reaction] = usernames
        }

        return map
    }

    private fun mapUrl(urls: List<UrlEntity>): List<Url> {
        val list = mutableListOf<Url>()

        urls.forEach { url ->
            val parsedUrl = url.hostname?.let {
                ParsedUrl(host = it)
            }
            val meta = if (!url.description.isNullOrEmpty() || !url.imageUrl.isNullOrEmpty() || !url.title.isNullOrEmpty()) {
                val raw = HashMap<String, String>()
                if (url.description != null) raw["ogDescription"] = url.description
                if (url.title != null) raw["ogTitle"] = url.title
                if (url.imageUrl != null) raw["ogImage"] = url.imageUrl
                Meta(title = url.title,description = url.description, imageUrl = url.imageUrl, raw = raw)
            } else null

            list.add(Url(url = url.url, meta = meta, parsedUrl = parsedUrl))
        }

        return list
    }

    private fun mapUser(user: UserEntity): SimpleUser {
        return with(user) {
            SimpleUser(
                id = id,
                username = username,
                name = name
            )
        }
    }

    private suspend fun mapAttachments(attachments: List<AttachmentEntity>): List<Attachment> {
        val list = mutableListOf<Attachment>()
        attachments.forEach { attachment ->
            with(attachment) {
                when {
                    imageUrl != null -> {
                        ImageAttachment(title, description, text, titleLink, titleLinkDownload, imageUrl, type, imageSize)
                    }
                    videoUrl != null -> {
                        VideoAttachment(title, description, text, titleLink, titleLinkDownload, videoUrl, type, videoSize)
                    }
                    audioUrl != null -> {
                        AudioAttachment(title, description, text, titleLink, titleLinkDownload, audioUrl, type, audioSize)
                    }
                    titleLink != null -> {
                        GenericFileAttachment(title, description, text, titleLink, titleLink, titleLinkDownload)
                    }
                    text != null && color != null && fallback != null -> {
                        ColorAttachment(Color.Custom(color), text, fallback)
                    }
                    text != null -> {
                        // TODO how to model message with attachments
                        MessageAttachment(authorName, authorIcon, text, thumbUrl,
                                color?.let { Color.Custom(it) }, messageLink, null, timestamp)
                    }
                    authorLink != null -> {
                        mapAuthorAttachment(this)
                    }
                    hasFields -> {
                        mapColorAttachmentWithFields(this)
                    }
                    hasActions -> {
                        mapActionAttachment(this)
                    }
                    else -> null
                }?.let { list.add(it) }
            }
        }
        return list
    }

    private suspend fun mapColorAttachmentWithFields(entity: AttachmentEntity): ColorAttachment {
        val fields = withContext(CommonPool) {
            dbManager.messageDao().getAttachmentFields(entity._id)
        }.map { Field(it.title, it.value) }
        return with(entity) {
            ColorAttachment(
                color = Color.Custom(color ?: DEFAULT_COLOR_STR),
                text = text ?: "",
                fallback = fallback,
                fields = fields)
        }
    }

    private suspend fun mapActionAttachment(attachment: AttachmentEntity): ActionsAttachment {
        val actions = withContext(CommonPool) {
            dbManager.messageDao().getAttachmentActions(attachment._id)
        }.mapNotNull { mapAction(it) }
        return with(attachment) {
            // TODO - remove the default "vertical" value from here...
            ActionsAttachment(title, actions, buttonAlignment ?: "vertical")
        }
    }

    private fun mapAction(action: AttachmentActionEntity): Action? {
        return when (action.type) {
            "button" -> ButtonAction(action.type, action.text, action.url, action.isWebView,
                    action.webViewHeightRatio, action.imageUrl, action.message,
                    action.isMessageInChatWindow)
            else -> null
        }
    }

    private suspend fun mapAuthorAttachment(attachment: AttachmentEntity): AuthorAttachment {
        val fields = withContext(CommonPool) {
            dbManager.messageDao().getAttachmentFields(attachment._id)
        }.map { Field(it.title, it.value) }
        return with(attachment) {
            AuthorAttachment(authorLink!!, authorIcon, authorName, fields)
        }
    }
}