package chat.rocket.android.db.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import chat.rocket.android.emoji.internal.db.StringListConverter

@Entity(tableName = "chatrooms",
        indices = [
            Index(value = ["userId"]),
            Index(value = ["ownerId"]),
            Index(value = ["subscriptionId"], unique = true),
            Index(value = ["updatedAt"]),
            Index(value = ["lastMessageUserId"])
        ],
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["ownerId"]),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"]),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["lastMessageUserId"])
        ]
)
@TypeConverters(StringListConverter::class)
data class ChatRoomEntity(
    @PrimaryKey var id: String,
    var subscriptionId: String,
    var type: String,
    var name: String,
    var fullname: String? = null,
    var userId: String? = null,
    var ownerId: String? = null,
    var readonly: Boolean? = false,
    var isDefault: Boolean? = false,
    var favorite: Boolean? = false,
    var topic: String? = null,
    var announcement: String? = null,
    var description: String? = null,
    var open: Boolean = true,
    var alert: Boolean = false,
    var unread: Long = 0,
    var userMentions: Long? = 0,
    var groupMentions: Long? = 0,
    var updatedAt: Long? = -1,
    var timestamp: Long? = -1,
    var lastSeen: Long? = -1,
    var lastMessageText: String? = null,
    var lastMessageUserId: String? = null,
    var lastMessageTimestamp: Long? = null,
    var broadcast: Boolean? = false,
    var muted: List<String>? = null
)

data class ChatRoom(
    @Embedded var chatRoom: ChatRoomEntity,
    var username: String?,
    var userFullname: String?,
    var status: String?,
    var lastMessageUserName: String?,
    var lastMessageUserFullName: String?
)
