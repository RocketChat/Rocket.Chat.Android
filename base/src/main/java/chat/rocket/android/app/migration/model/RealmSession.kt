package chat.rocket.android.app.migration.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmSession : RealmObject() {
    @JvmField
    @PrimaryKey
    var sessionId: Int = 0 //only 0 is used!
    @JvmField
    var token: String? = null
    @JvmField
    var tokenVerified: Boolean = false
    @JvmField
    var error: String? = null
}