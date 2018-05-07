package chat.rocket.android.app.migration.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmPublicSetting : RealmObject() {
    @PrimaryKey
    @JvmField
    var _id: String? = null
    @JvmField
    var group: String? = null
    @JvmField
    var type: String? = null
    @JvmField
    var value: String? = null
    @JvmField
    var _updatedAt: Long = 0
    @JvmField
    var meta: String? = null
}