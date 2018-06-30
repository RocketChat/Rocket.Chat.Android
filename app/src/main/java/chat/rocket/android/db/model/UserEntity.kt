package chat.rocket.android.db.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users",
        indices = [Index(value = ["username"])])
data class UserEntity(
    @PrimaryKey override val id: String,
    var username: String? = null,
    var name: String? = null,
    override var status: String = "offline",
    var utcOffset: Float? = null
) : BaseUserEntity

data class UserStatus(
    override val id: String,
    override val status: String
) : BaseUserEntity

interface BaseUserEntity {
    val id: String
    val status: String
}