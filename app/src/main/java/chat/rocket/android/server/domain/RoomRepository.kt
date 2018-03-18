package chat.rocket.android.server.domain

import chat.rocket.common.model.RoomType
import chat.rocket.core.model.Room

interface RoomRepository {
    /**
     * Get all rooms. Use carefully!
     *
     * @return All rooms or an empty list.
     */
    fun getAll(): List<Room>

    fun get(query: Query.() -> Unit): List<Room>
    /**
     * Save a single room object.
     *
     * @param room The room object to save.
     */
    fun save(room: Room)

    /**
     * Save a list of rooms.
     *
     * @param roomList The list of rooms to save.
     */
    fun saveAll(roomList: List<Room>)

    /**
     * Removes all rooms.
     */
    fun clear()

    data class Query(
            var id: String? = null,
            var name: String? = null,
            var fullName: String? = null,
            var type: RoomType? = null,
            var readonly: Boolean? = null
    )
}