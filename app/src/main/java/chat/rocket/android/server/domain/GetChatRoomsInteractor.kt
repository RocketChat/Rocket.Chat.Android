package chat.rocket.android.server.domain

import chat.rocket.core.model.ChatRoom
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.withContext
import javax.inject.Inject

class GetChatRoomsInteractor @Inject constructor(private val repository: ChatRoomsRepository) {

    /**
     * Get all ChatRoom objects.
     *
     * @param url The server url.
     *
     * @return All the ChatRoom objects.
     */
    fun get(url: String) = repository.get(url)

    /**
     * Get a list of chat rooms that contains the name parameter.
     *
     * @param url The server url.
     * @param name The name of chat room to look for or a chat room that contains this name.
     * @return A list of ChatRoom objects with the given name.
     */
    suspend fun getByName(url: String, name: String): List<ChatRoom> = withContext(CommonPool) {
        val allChatRooms = repository.get(url)
        if (name.isEmpty()) {
            return@withContext allChatRooms
        }
        return@withContext allChatRooms.filter {
            it.name.contains(name, true)
        }
    }

    /**
     * Get a specific room by its id.
     *
     * @param serverUrl The server url where the room is.
     * @param roomId The id of the room to get.
     * @return The ChatRoom object or null if we couldn't find any.
     */
    suspend fun getById(serverUrl: String, roomId: String): ChatRoom? = withContext(CommonPool) {
        val allChatRooms = repository.get(serverUrl)
        return@withContext allChatRooms.first {
            it.id == roomId
        }
    }
}