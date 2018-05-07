package chat.rocket.android.app.migration.model

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class RealmPreferences : RealmObject() {
    @PrimaryKey
    @JvmField
    var id: String? = null
    @JvmField
    var newRoomNotification: String? = null
    @JvmField
    var newMessageNotification: String? = null
    @JvmField
    var useEmojis: Boolean = false
    @JvmField
    var convertAsciiEmoji: Boolean = false
    @JvmField
    var saveMobileBandwidth: Boolean = false
    @JvmField
    var collapseMediaByDefault: Boolean = false
    @JvmField
    var unreadRoomsMode: Boolean = false
    @JvmField
    var autoImageLoad: Boolean = false
    @JvmField
    var emailNotificationMode: String? = null
    @JvmField
    var unreadAlert: Boolean = false
    @JvmField
    var desktopNotificationDuration: Int = 0
    @JvmField
    var viewMode: Int = 0
    @JvmField
    var hideUsernames: Boolean = false
    @JvmField
    var hideAvatars: Boolean = false
    @JvmField
    var hideFlexTab: Boolean = false
}