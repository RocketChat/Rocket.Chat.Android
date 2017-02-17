package chat.rocket.core.interactors;

import io.reactivex.Flowable;

import java.util.List;
import chat.rocket.core.models.Room;
import chat.rocket.core.repositories.RoomRepository;

public class RoomInteractor {

  private final RoomRepository roomRepository;

  public RoomInteractor(RoomRepository roomRepository) {
    this.roomRepository = roomRepository;
  }

  public Flowable<Integer> getTotalUnreadMentionsCount() {
    return roomRepository.getAll()
        .flatMap(rooms -> Flowable.fromIterable(rooms)
            .filter(room -> room.isOpen() && room.isAlert())
            .map(Room::getUnread)
            .defaultIfEmpty(0)
            .reduce((unreadCount, unreadCount2) -> unreadCount + unreadCount2)
            .toFlowable());
  }

  public Flowable<Long> getTotalUnreadRoomsCount() {
    return roomRepository.getAll()
        .flatMap(rooms -> Flowable.fromIterable(rooms)
            .filter(room -> room.isOpen() && room.isAlert())
            .count()
            .toFlowable());
  }

  public Flowable<List<Room>> getOpenRooms() {
    return roomRepository.getAll()
        .flatMap(rooms -> Flowable.fromIterable(rooms)
            .filter(Room::isOpen)
            .toList()
            .toFlowable());
  }
}
