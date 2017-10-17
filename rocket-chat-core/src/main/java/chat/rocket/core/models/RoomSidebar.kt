package chat.rocket.core.models

class RoomSidebar {
    lateinit var id: String
    lateinit var roomName: String
    lateinit var roomId: String
    var type: String? = null
        get() = RoomType.get(field) ?: Room.TYPE_GROUP
    var userStatus: String? = null
    var isAlert: Boolean = false
    var isFavorite: Boolean = false
    var unread: Int = 0
    var updateAt: Long = 0
    var lastSeen: Long = 0
}