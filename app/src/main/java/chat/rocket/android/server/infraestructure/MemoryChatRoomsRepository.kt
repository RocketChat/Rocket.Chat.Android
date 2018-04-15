package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.ChatRoomsRepository
import chat.rocket.android.widget.roomupdate.UpdateObserver
import chat.rocket.core.model.ChatRoom
import chat.rocket.common.model.roomTypeOf
import timber.log.Timber

class MemoryChatRoomsRepository : ChatRoomsRepository {
    private val repoListeners = ArrayList<UpdateObserver>()
    val cache = HashMap<String, List<ChatRoom>>()

    override fun save(url: String, chatRooms: List<ChatRoom>) {
        //TODO: should diff the existing chatrooms and new chatroom dataset
        cache[url] = chatRooms
        notifyObservers(chatRooms)
    }

    override fun get(url: String): List<ChatRoom> = cache[url] ?: emptyList()

    override fun registerObserver(observer: UpdateObserver) {
        if (!repoListeners.contains(observer))
            repoListeners.add(observer)
    }

    override fun removeObserver(observer: UpdateObserver) {
        if (repoListeners.contains(observer))
            repoListeners.remove(observer)
    }

    override fun notifyObservers(rooms: List<ChatRoom>) {
        repoListeners.forEach {
            val roomId = it.provideRoomId()

            val room = rooms.find { it.id == roomId }

            if (room != null && it.lastUpdated() != room.updatedAt)
                it.onRoomChanged(room.name, room.type, room.readonly, room.updatedAt)
        }
    }

}