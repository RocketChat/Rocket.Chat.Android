package chat.rocket.android.db.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "reactions")
data class ReactionEntity(
    @PrimaryKey val reaction: String
) : BaseMessageEntity

@Entity(tableName = "reactions_message_relations",
        foreignKeys = [
            ForeignKey(entity = ReactionEntity::class, parentColumns = ["reaction"],
                    childColumns = ["reactionId"]),
            ForeignKey(entity = MessageEntity::class, parentColumns = ["id"],
                    childColumns = ["messageId"], onDelete = ForeignKey.CASCADE)
        ],
        indices = [
            Index(value = ["messageId"])
        ]
)
data class ReactionMessageRelation(
    val reactionId: String,
    val messageId: String,
    val count: Int
) : BaseMessageEntity {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
}