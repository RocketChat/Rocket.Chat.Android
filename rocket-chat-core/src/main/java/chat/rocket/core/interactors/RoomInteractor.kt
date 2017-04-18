package chat.rocket.core.interactors

import io.reactivex.Flowable
import chat.rocket.core.SortDirection
import chat.rocket.core.models.Room
import chat.rocket.core.repositories.RoomRepository

class RoomInteractor(private val roomRepository: RoomRepository) {

    fun getTotalUnreadMentionsCount(): Flowable<Int> {
        return roomRepository.all
                .flatMap { rooms ->
                    Flowable.fromIterable(rooms)
                            .filter { room -> room.isOpen && room.isAlert }
                            .map { it.unread }
                            .defaultIfEmpty(0)
                            .reduce { unreadCount, unreadCount2 -> unreadCount + unreadCount2 }
                            .toFlowable()
                }
    }

    fun getTotalUnreadRoomsCount(): Flowable<Long> {
        return roomRepository.all
                .flatMap { rooms ->
                    Flowable.fromIterable(rooms)
                            .filter { room -> room.isOpen && room.isAlert }
                            .count()
                            .toFlowable()
                }
    }

    fun getOpenRooms(): Flowable<List<Room>> {
        return roomRepository.all
                .flatMap { rooms ->
                    Flowable.fromIterable(rooms)
                            .filter { it.isOpen }
                            .toList()
                            .toFlowable()
                }
    }

    fun getRoomsWithNameLike(name: String): Flowable<List<Room>> {
        return roomRepository.getSortedLikeName(name, SortDirection.DESC, 5)
    }
}
