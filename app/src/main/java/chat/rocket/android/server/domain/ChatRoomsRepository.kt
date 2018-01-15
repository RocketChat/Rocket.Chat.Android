package chat.rocket.android.server.domain

import chat.rocket.core.model.ChatRoom

interface ChatRoomsRepository {

    fun save(url: String, chatRooms: List<ChatRoom>)

    fun get(url: String): List<ChatRoom>
}