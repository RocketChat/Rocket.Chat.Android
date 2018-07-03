package chat.rocket.android.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "attachments",
        foreignKeys = [
            ForeignKey(entity = MessageEntity::class, parentColumns = ["id"],
                    childColumns = ["message_id"], onDelete = ForeignKey.CASCADE)
        ])
data class AttachmentEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo(name = "message_id")
    val messageId: String,
    val title: String?,
    val type: String?,
    val description: String?,
    val text: String?,
    @ColumnInfo(name = "author_name")
    val authorName: String?,
    @ColumnInfo(name = "author_icon")
    val authorIcon: String?,
    @ColumnInfo(name = "author_link")
    val authorLink: String?,
    @ColumnInfo(name = "thumb_url")
    val thumbUrl: String?,
    val color: String?,
    @ColumnInfo(name = "title_link")
    val titleLink: String?,
    @ColumnInfo(name = "title_link_download")
    val titleLinkDownload: String?,
    @ColumnInfo(name = "image_url")
    val imageUrl: String?,
    @ColumnInfo(name = "image_type")
    val imageType: String?,
    @ColumnInfo(name = "image_size")
    val imageSize: String?,
    @ColumnInfo(name = "video_url")
    val videoUrl: String?,
    @ColumnInfo(name = "video_type")
    val videoType: String?,
    @ColumnInfo(name = "video_size")
    val videoSize: String?,
    @ColumnInfo(name = "audio_url")
    val audioUrl: String?,
    @ColumnInfo(name = "audio_type")
    val audioType: String?,
    @ColumnInfo(name = "audio_size")
    val audioSize: String?,
    @ColumnInfo(name = "message_link")
    val messageLink: String?,
    val timestamp: Long?
) : BaseMessageEntity

@Entity(tableName = "attachment_fields",
        foreignKeys = [
            ForeignKey(entity = AttachmentEntity::class, parentColumns = ["id"],
                    childColumns = ["attachmentId"], onDelete = ForeignKey.CASCADE)
        ])
data class AttachmentFieldEntity(
    val attachmentId: String,
    val title: String,
    val value: String
) : BaseMessageEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}