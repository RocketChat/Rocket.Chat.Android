package chat.rocket.android.app.migration.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmSettings : RealmObject() {
    @PrimaryKey
    @JvmField
    var id: String? = null
    @JvmField
    var preferences: RealmPreferences? = null
}