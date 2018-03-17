package chat.rocket.android.room.weblink

import android.arch.persistence.room.Entity
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "weblink", indices = [(Index(value = ["link"], unique = true))])
data class WebLinkEntity(
        val name: String = "",
        @PrimaryKey
        val link: String
)
