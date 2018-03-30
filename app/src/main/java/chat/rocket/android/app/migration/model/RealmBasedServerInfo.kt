package chat.rocket.android.app.migration.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmBasedServerInfo : RealmObject() {
    @PrimaryKey
    @JvmField var hostname: String? = null
    @JvmField var name: String? = null
    @JvmField var session: String? = null
    @JvmField var insecure: Boolean = false
}