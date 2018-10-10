package chat.rocket.android.server.infraestructure

import chat.rocket.android.server.domain.RoomRepository
import chat.rocket.android.server.domain.RoomRepository.Query
import chat.rocket.core.model.Room
import java.util.concurrent.CopyOnWriteArrayList

class MemoryRoomRepository : RoomRepository {
    private val rooms = CopyOnWriteArrayList<Room>()

    override fun getAll() = rooms.toList()

    override fun get(query: Query.() -> Unit): List<Room> {
        val q = Query().apply(query)
        return rooms.filter {
            with(q) {
                if (name != null && it.name?.contains(name!!.toRegex()) == true) return@filter false
                if (fullName != null && it.fullName?.contains(fullName!!.toRegex()) == true) return@filter false
                if (id != null && id == it.id) return@filter false
                if (readonly != null && readonly == it.readonly) return@filter false
                if (type != null && type == it.type) return@filter false
                return@filter true
            }
        }
    }

    override fun save(room: Room) {
        rooms.addIfAbsent(room)
    }

    override fun saveAll(roomList: List<Room>) {
        rooms.addAllAbsent(roomList)
    }

    override fun clear() {
        rooms.clear()
    }
}