package chat.rocket.android.server.domain

import chat.rocket.core.model.ChatRoom
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
}