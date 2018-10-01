package chat.rocket.android.db.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

interface BaseMessageEntity

@Entity(tableName = "messages",
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["senderId"]),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["editedBy"])
        ])
data class MessageEntity(
    @PrimaryKey val id: String,
    val roomId: String,
    val message: String,
    val timestamp: Long,
    val senderId: String?,
    val updatedAt: Long?,
    val editedAt: Long?,
    val editedBy: String?,
    val senderAlias: String?,
    val avatar: String?,
    val type: String?,
    val groupable: Boolean = false,
    val parseUrls: Boolean = false,
    val pinned: Boolean = false,
    val role: String?,
    val synced: Boolean = true,
    val unread: Boolean? = null
) : BaseMessageEntity

@Entity(tableName = "message_favorites",
        primaryKeys = ["messageId", "userId"],
        foreignKeys = [
            ForeignKey(entity = MessageEntity::class, parentColumns = ["id"],
                    childColumns = ["messageId"], onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"])
        ])
data class MessageFavoritesRelation(
    val messageId: String,
    val userId: String
) : BaseMessageEntity

@Entity(tableName = "message_mentions",
        primaryKeys = ["messageId", "userId"],
        foreignKeys = [
            ForeignKey(entity = MessageEntity::class, parentColumns = ["id"],
                    childColumns = ["messageId"], onDelete = ForeignKey.CASCADE),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"])
        ])
data class MessageMentionsRelation(
    val messageId: String,
    val userId: String
) : BaseMessageEntity

@Entity(tableName = "message_channels",
        primaryKeys = ["messageId", "roomId"],
        foreignKeys = [
            ForeignKey(entity = MessageEntity::class, parentColumns = ["id"],
                    childColumns = ["messageId"], onDelete = ForeignKey.CASCADE)
        ]
)
data class MessageChannels(
    val messageId: String,
    val roomId: String,
    val roomName: String?
) : BaseMessageEntity

@Entity(tableName = "messages_sync")
data class MessagesSync(
    @PrimaryKey val roomId: String,
    val timestamp: Long
)

data class PartialMessage(
    @Embedded val message: MessageEntity,
    val senderName: String?,
    val senderUsername: String?,
    val editName: String?,
    val editUsername: String?
) {
    @Relation(parentColumn = "id", entityColumn = "messageId")
    var urls: List<UrlEntity>? = null
    @Relation(parentColumn = "id", entityColumn = "message_id")
    var attachments: List<AttachmentEntity>? = null
    @Relation(parentColumn = "id", entityColumn = "messageId")
    var reactions: List<ReactionEntity>? = null
    @Relation(parentColumn = "id", entityColumn = "messageId")
    var channels: List<MessageChannels>? = null

    override fun toString(): String {
        return "PartialMessage(message=$message, senderName=$senderName, senderUsername=$senderUsername, editName=$editName, editUsername=$editUsername, urls=$urls, attachments=$attachments, reactions=$reactions, channels=$channels)"
    }


}

data class FullMessage(
    val message: PartialMessage,
    val favorites: List<UserEntity>,
    val mentions: List<UserEntity>
)
