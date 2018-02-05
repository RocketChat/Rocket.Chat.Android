package chat.rocket.android.server.domain

import chat.rocket.core.model.ChatRoom
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.async
import javax.inject.Inject

class GetChatRoomsInteractor @Inject constructor(private val repository: ChatRoomsRepository) {
    fun get(url: String) = repository.get(url)

    suspend fun getByName(url: String, name: String): List<ChatRoom> {
        val chatRooms = async {
            val allChatRooms = repository.get(url)
            if (name.isEmpty()) {
                return@async allChatRooms
            }
            return@async allChatRooms.filter {
                it.name.contains(name, true)
            }
        }
        return chatRooms.await()
    }

    /**
     * Get a specific room by its id.
     *
     * @param serverUrl The server url where the room is.
     * @param roomId The id of the room to get.
     *
     * @return The ChatRoom object or null if we couldn't find any.
     */
    suspend fun getByRoomId(serverUrl: String, roomId: String): ChatRoom {
        return async(CommonPool) {
            val allChatRooms = repository.get(serverUrl)
            return@async allChatRooms.first {
                it.id == roomId
            }
        }.await()
    }
}