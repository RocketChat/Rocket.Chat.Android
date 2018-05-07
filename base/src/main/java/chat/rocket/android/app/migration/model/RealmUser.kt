package chat.rocket.android.app.migration.model

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmUser : RealmObject() {

    companion object {
        const val ID = "_id"
        const val NAME = "name"
        const val USERNAME = "username"
        const val STATUS = "status"
        const val UTC_OFFSET = "utcOffset"
        const val EMAILS = "emails"
        const val SETTINGS = "settings"
        const val STATUS_ONLINE = "online"
        const val STATUS_BUSY = "busy"
        const val STATUS_AWAY = "away"
        const val STATUS_OFFLINE = "offline"
    }

    @PrimaryKey
    @JvmField
    var _id: String? = null
    @JvmField
    var name: String? = null
    @JvmField
    var username: String? = null
    @JvmField
    var status: String? = null
    @JvmField
    var utcOffset: Double = 0.toDouble()
    @JvmField
    var emails: RealmList<RealmEmail>? = null
    @JvmField
    var settings: RealmSettings? = null
}