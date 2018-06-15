package chat.rocket.android.db.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "chatrooms",
        indices = [
            Index(value = ["userId"]),
            Index(value = ["ownerId"]),
            Index(value = ["subscriptionId"], unique = true),
            Index(value = ["updatedAt"])
        ],
        foreignKeys = [
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["ownerId"]),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["userId"]),
            ForeignKey(entity = UserEntity::class, parentColumns = ["id"], childColumns = ["lastMessageUserId"])
        ]
)
data class ChatRoomEntity(
    @PrimaryKey var id: String,
    var subscriptionId: String,
    var type: String,
    var name: String,
    var fullname: String?,
    var userId: String?,
    var ownerId: String?,
    var readonly: Boolean? = false,
    var isDefault: Boolean? = false,
    var favorite: Boolean? = false,
    var open: Boolean = true,
    var alert: Boolean = false,
    var unread: Long = 0,
    var userMentions: Long? = 0,
    var groupMentions: Long? = 0,
    var updatedAt: Long? = -1,
    var timestamp: Long? = -1,
    var lastSeen: Long? = -1,
    var lastMessageText: String?,
    var lastMessageUserId: String?,
    var lastMessageTimestamp: Long?
)

data class ChatRoom(
    @Embedded var chatRoom: ChatRoomEntity,
    var username: String?,
    var userFullname: String?,
    var status: String?,
    var lastMessageUserName: String?,
    var lastMessageUserFullName: String?
)