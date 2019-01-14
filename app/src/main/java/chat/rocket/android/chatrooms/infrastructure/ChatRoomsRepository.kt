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
    }
}

fun ChatRoomsRepository.Order.isGrouped(): Boolean = this == ChatRoomsRepository.Order.GROUPED_ACTIVITY
        || this == ChatRoomsRepository.Order.GROUPED_NAME