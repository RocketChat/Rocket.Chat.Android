package chat.rocket.android.server.domain

import chat.rocket.core.model.ChatRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRoomsInteractor @Inject constructor(private val repository: ChatRoomsRepository) {

    /**
     * Get all [ChatRoom].
     *
     * @param url The server url.
     *
     * @return All the [ChatRoom] objects.
     */
    fun getAll(url: String) = repository.get(url)

    /**
     * Get a list of chat rooms that contains the name parameter.
     *
     * @param url The server url.
     * @param name The name of chat room to look for or a chat room that contains this name.
     * @return A list of ChatRoom objects with the given name.
     */
    suspend fun getAllByName(url: String, name: String): List<ChatRoom> =
        withContext(Dispatchers.IO) {
            val allChatRooms = repository.get(url)
            if (name.isEmpty()) {
                return@withContext allChatRooms
            }
            return@withContext allChatRooms.filter {
                it.name.contains(name, true)
            }
        }

    /**
     * Get a specific [ChatRoom] by its id.
     *
     * @param serverUrl The server url where the room is.
     * @param roomId The id of the room to get.
     * @return The [ChatRoom] object or null if we couldn't find any.
     */
    suspend fun getById(serverUrl: String, roomId: String): ChatRoom? =
        withContext(Dispatchers.IO) {
            return@withContext repository.get(serverUrl).find {
                it.id == roomId
            }
        }

    /**
     * Get a specific [ChatRoom] by its name.
     *
     * @param serverUrl The server url where the room is.
     * @param name The name of the room to get.
     * @return The [ChatRoom] object or null if we couldn't find any.
     */
    fun getByName(serverUrl: String, name: String): ChatRoom? {
        return getAll(serverUrl).firstOrNull { it.name == name || it.fullName == name }
    }

    /**
     * Add a [ChatRoom].
     *
     * @param url The server url.
     * @param chatRoom The [ChatRoom] to be added to the list.
     */
    fun add(url: String, chatRoom: ChatRoom) {
        val chatRooms: MutableList<ChatRoom> = getAll(url).toMutableList()
        synchronized(this) {
            chatRooms.add(chatRoom)
        }
        repository.save(url, chatRooms)
    }

    /**
     * Removes a [ChatRoom].
     *
     * @param url The server url.
     * @param chatRoom The [ChatRoom] to be removed from the list.
     */
    fun remove(url: String, chatRoom: ChatRoom) {
        val chatRooms: MutableList<ChatRoom> = getAll(url).toMutableList()
        synchronized(this) {
            chatRooms.removeAll { chatRoom_ -> chatRoom_.id == chatRoom.id }
        }
        repository.save(url, chatRooms)
    }
}