package chat.rocket.android.db.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "reactions",
        foreignKeys = [
            ForeignKey(entity = MessageEntity::class, parentColumns = ["id"],
                    childColumns = ["messageId"], onDelete = ForeignKey.CASCADE)
        ],
        indices = [
            Index(value = ["messageId"])
        ]
)
data class ReactionEntity(
    @PrimaryKey val reaction: String,
    val messageId: String,
    val count: Int,
    val usernames: String
) : BaseMessageEntity