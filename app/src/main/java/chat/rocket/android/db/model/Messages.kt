package chat.rocket.android.db.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    val role: String?
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
data class MessageChannelsRelation(
    val messageId: String,
    val roomId: String,
    val roomName: String?
) : BaseMessageEntity
