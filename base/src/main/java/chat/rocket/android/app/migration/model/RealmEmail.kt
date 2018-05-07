package chat.rocket.android.app.migration.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmEmail : RealmObject() {
    @PrimaryKey
    @JvmField
    var address: String? = null
    @JvmField
    var verified: Boolean = false
}