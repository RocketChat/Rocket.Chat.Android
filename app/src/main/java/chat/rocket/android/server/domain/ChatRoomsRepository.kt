package chat.rocket.android.server.domain

import chat.rocket.android.widget.roomupdate.UpdateObserver
import chat.rocket.core.model.ChatRoom

interface ChatRoomsRepository {

    fun registerObserver(observer: UpdateObserver)

    fun removeObserver(observer: UpdateObserver)

    fun notifyObservers(rooms: List<ChatRoom>)

    fun save(url: String, chatRooms: List<ChatRoom>)

    fun get(url: String): List<ChatRoom>
}