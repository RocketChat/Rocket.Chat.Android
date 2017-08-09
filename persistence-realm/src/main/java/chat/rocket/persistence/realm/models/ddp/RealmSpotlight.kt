package chat.rocket.persistence.realm.models.ddp

import chat.rocket.core.models.Spotlight
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

// This class must be annotated with open to work properly with Realm (Kotlin classes are final by default).
open class RealmSpotlight : RealmObject() {
    @PrimaryKey var _id: String? = null
    var name: String? = null
    var type: String? = null
    var username: String? = null
    var status: String? = null

    fun asSpotlight(): Spotlight {
        return Spotlight.builder()
                .setId(_id)
                .setName(name)
                .setType(type)
                .setUsername(username)
                .setStatus(status)
                .build()
    }

    interface Columns {
        companion object {
            const val ID = "_id"
            const val NAME = "name"
            const val TYPE = "t"
            const val USERNAME = "username"
            const val STATUS = "status"
        }
    }
}