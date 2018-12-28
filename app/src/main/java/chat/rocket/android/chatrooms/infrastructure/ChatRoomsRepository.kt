package chat.rocket.android.chatrooms.infrastructure

import androidx.lifecycle.LiveData
import chat.rocket.android.db.ChatRoomDao
import chat.rocket.android.db.model.ChatRoom
import chat.rocket.android.util.retryDB
import javax.inject.Inject

class ChatRoomsRepository @Inject constructor(private val dao: ChatRoomDao) {

    // TODO - check how to use retryDB here - suspend
    fun getChatRooms(order: Order): LiveData<List<ChatRoom>> {
        return when(order) {
            Order.ACTIVITY -> dao.getAll()
            Order.GROUPED_ACTIVITY -> dao.getAllGrouped()
            Order.NAME -> dao.getAllAlphabetically()
            Order.GROUPED_NAME -> dao.getAllAlphabeticallyGrouped()
            Order.UNREAD_ACTIVITY -> dao.getAllUnread()
            Order.GROUPED_UNREAD_ACTIVITY -> dao.getAllUnreadGrouped()
            Order.UNREAD_NAME -> dao.getAllUnreadAlphabetically()
            Order.GROUPED_UNREAD_NAME -> dao.getAllUnreadAlphabeticallyGrouped()
        }
    }

    suspend fun search(query: String) =
        retryDB("roomSearch($query)") { dao.searchSync(query) }

    suspend fun count() = retryDB("roomsCount") { dao.count() }

    enum class Order {
        ACTIVITY,
        GROUPED_ACTIVITY,
        NAME,
        GROUPED_NAME,
        UNREAD_ACTIVITY,
        GROUPED_UNREAD_ACTIVITY,
        UNREAD_NAME,
        GROUPED_UNREAD_NAME,
    }
}

fun ChatRoomsRepository.Order.isGrouped(): Boolean = this == ChatRoomsRepository.Order.GROUPED_ACTIVITY
        || this == ChatRoomsRepository.Order.GROUPED_NAME || this == ChatRoomsRepository.Order.GROUPED_UNREAD_ACTIVITY
        || this == ChatRoomsRepository.Order.GROUPED_UNREAD_NAME