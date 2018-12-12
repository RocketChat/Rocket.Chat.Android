package chat.rocket.android.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class ContactEntity(
        @PrimaryKey(autoGenerate = true)
        var id: Int,
        var name: String? = null,
        var username: String? = null,
        var phoneNumber: String? = null,
        var emailAddress: String? = null,
        var isPhone: Boolean = true

)