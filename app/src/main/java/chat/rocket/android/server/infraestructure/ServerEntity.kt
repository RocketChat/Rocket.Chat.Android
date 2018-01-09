package chat.rocket.android.server.infraestructure

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "server", indices = [(Index(value = ["host"], unique = true))])
data class ServerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val host: String,
    val avatar: String
)