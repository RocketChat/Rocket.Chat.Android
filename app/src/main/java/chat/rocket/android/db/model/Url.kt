package chat.rocket.android.db.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "urls",
        foreignKeys = [
            ForeignKey(entity = MessageEntity::class, parentColumns = ["id"],
                    childColumns = ["messageId"], onDelete = ForeignKey.CASCADE)
        ],
        indices = [
            Index(value = ["messageId"])
        ])
data class UrlEntity(
    val messageId: String,
    val url: String,
    val hostname: String?,
    val title: String?,
    val description: String?,
    val imageUrl: String?
) : BaseMessageEntity {
    @PrimaryKey(autoGenerate = true)
    var urlId: Long? = null
}