package chat.rocket.core.interactors;

import java.util.List;
import chat.rocket.core.models.Room;
import chat.rocket.core.repositories.RoomRepository;
import rx.Observable;

public class RoomInteractor {

  private final RoomRepository roomRepository;

  public RoomInteractor(RoomRepository roomRepository) {
    this.roomRepository = roomRepository;
  }

  public Observable<Integer> getTotalUnreadMentionsCount() {
    return roomRepository.getAll()
        .flatMap(rooms -> Observable.from(rooms)
            .filter(room -> room.isOpen() && room.isAlert())
            .map(Room::getUnread)
            .defaultIfEmpty(0)
            .reduce((unreadCount, unreadCount2) -> unreadCount + unreadCount2));
  }

  public Observable<Integer> getTotalUnreadRoomsCount() {
    return roomRepository.getAll()
        .flatMap(rooms -> Observable.from(rooms)
            .filter(room -> room.isOpen() && room.isAlert())
            .count());
  }

  public Observable<List<Room>> getOpenRooms() {
    return roomRepository.getAll()
        .flatMap(rooms -> Observable.from(rooms)
            .filter(Room::isOpen)
            .toList());
  }
}
