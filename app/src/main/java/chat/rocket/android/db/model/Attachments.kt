package chat.rocket.android.db.model

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import chat.rocket.android.R
import chat.rocket.android.util.extension.orFalse
import chat.rocket.android.util.extensions.isNotNullNorEmpty
import chat.rocket.core.model.attachment.Attachment
import chat.rocket.core.model.attachment.actions.ButtonAction

@Entity(tableName = "attachments",
        foreignKeys = [
            ForeignKey(entity = MessageEntity::class, parentColumns = ["id"],
                    childColumns = ["message_id"], onDelete = ForeignKey.CASCADE)
        ])
data class AttachmentEntity(
    @PrimaryKey
    var _id: String,
    @ColumnInfo(name = "message_id")
    val messageId: String,
    val title: String? = null,
    val type: String? = null,
    val description: String? = null,
    val text: String? = null,
    @ColumnInfo(name = "author_name")
    val authorName: String? = null,
    @ColumnInfo(name = "author_icon")
    val authorIcon: String? = null,
    @ColumnInfo(name = "author_link")
    val authorLink: String? = null,
    @ColumnInfo(name = "thumb_url")
    val thumbUrl: String? = null,
    val color: String? = null,
    val fallback: String? = null,
    @ColumnInfo(name = "title_link")
    val titleLink: String? = null,
    @ColumnInfo(name = "title_link_download")
    val titleLinkDownload: Boolean = false,
    @ColumnInfo(name = "image_url")
    val imageUrl: String? = null,
    @ColumnInfo(name = "image_type")
    val imageType: String? = null,
    @ColumnInfo(name = "image_size")
    val imageSize: Long? = null,
    @ColumnInfo(name = "video_url")
    val videoUrl: String? = null,
    @ColumnInfo(name = "video_type")
    val videoType: String? = null,
    @ColumnInfo(name = "video_size")
    val videoSize: Long? = null,
    @ColumnInfo(name = "audio_url")
    val audioUrl: String? = null,
    @ColumnInfo(name = "audio_type")
    val audioType: String? = null,
    @ColumnInfo(name = "audio_size")
    val audioSize: Long? = null,
    @ColumnInfo(name = "message_link")
    val messageLink: String? = null,
    val timestamp: Long? = null,
    @ColumnInfo(name = "has_actions")
    val hasActions: Boolean = false,
    @ColumnInfo(name = "has_fields")
    val hasFields: Boolean = false,
    @ColumnInfo(name = "button_alignment")
    val buttonAlignment: String? = null
) : BaseMessageEntity

@Entity(tableName = "attachment_fields",
        foreignKeys = [
            ForeignKey(entity = AttachmentEntity::class, parentColumns = ["_id"],
                    childColumns = ["attachmentId"], onDelete = ForeignKey.CASCADE)
        ],
        indices = [
            Index(value = ["attachmentId"])
        ])
data class AttachmentFieldEntity(
    val attachmentId: String,
    val title: String,
    val value: String
) : BaseMessageEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}

@Entity(tableName = "attachment_action",
        foreignKeys = [
            ForeignKey(entity = AttachmentEntity::class, parentColumns = ["_id"],
                    childColumns = ["attachmentId"], onDelete = ForeignKey.CASCADE)
        ],
        indices = [
            Index(value = ["attachmentId"])
        ])
data class AttachmentActionEntity(
    val attachmentId: String,
    val type: String,
    val text: String? = null,
    val url: String? = null,
    val isWebView: Boolean? = null,
    val webViewHeightRatio: String? = null,
    val imageUrl: String? = null,
    val message: String? = null,
    val isMessageInChatWindow: Boolean? = null
) : BaseMessageEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}

fun Attachment.asEntity(msgId: String, context: Context): List<BaseMessageEntity> {
    val attachmentId = "${msgId}_${hashCode()}"
    val list = mutableListOf<BaseMessageEntity>()

    val text = mapAttachmentText(text, attachments?.firstOrNull(), context)

    list.add(AttachmentEntity(
            _id = attachmentId,
            messageId = msgId,
            title = title,
            type = type,
            description = description,
            text = text,
            titleLink = titleLink,
            titleLinkDownload = titleLinkDownload.orFalse(),
            imageUrl = imageUrl,
            imageType = imageType,
            imageSize = imageSize,
            videoUrl = videoUrl,
            videoType = videoType,
            videoSize = videoSize,
            audioUrl = audioUrl,
            audioType = audioType,
            audioSize = audioSize,
            authorLink = authorLink,
            authorIcon = authorIcon,
            authorName = authorName,
            color = color?.rawColor,
            fallback = fallback,
            thumbUrl = thumbUrl,
            messageLink = messageLink,
            timestamp = timestamp,
            buttonAlignment = buttonAlignment,
            hasActions = actions?.isNotEmpty() == true,
            hasFields = fields?.isNotEmpty() == true
    ))

    fields?.forEach { field ->
        list.add(AttachmentFieldEntity(
                attachmentId = attachmentId,
                title = field.title,
                value = field.value
        ))
    }

    actions?.forEach { action ->
        when (action) {
            is ButtonAction -> AttachmentActionEntity(
                attachmentId = attachmentId,
                type = action.type,
                text = action.text,
                url = action.url,
                isWebView = action.isWebView,
                webViewHeightRatio = action.webViewHeightRatio,
                imageUrl = action.imageUrl,
                message = action.message,
                isMessageInChatWindow = action.isMessageInChatWindow
            )
            else -> null
        }?.let { list.add(it) }
    }
    return list
}

fun mapAttachmentText(text: String?, attachment: Attachment?, context: Context): String?  = attachment?.run {
    when {
        imageUrl.isNotNullNorEmpty() -> context.getString(R.string.msg_preview_photo)
        videoUrl.isNotNullNorEmpty() -> context.getString(R.string.msg_preview_video)
        audioUrl.isNotNullNorEmpty() -> context.getString(R.string.msg_preview_audio)
        titleLink.isNotNullNorEmpty() &&
                type?.contentEquals("file") == true ->
            context.getString(R.string.msg_preview_file)
        else -> text
    }
} ?: text