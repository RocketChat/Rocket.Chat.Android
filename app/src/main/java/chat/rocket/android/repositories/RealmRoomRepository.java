package chat.rocket.android.repositories;

import io.realm.Realm;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.model.core.Room;
import chat.rocket.android.model.ddp.RoomSubscription;
import rx.Observable;

public class RealmRoomRepository implements RoomRepository {

  private final Realm realm;

  public RealmRoomRepository(Realm realm) {
    this.realm = realm;
  }

  @Override
  public Observable<List<Room>> getOpenRooms() {
    return realm.where(RoomSubscription.class)
        .equalTo(RoomSubscription.OPEN, true)
        .findAllAsync()
        .asObservable()
        .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
            && roomSubscriptions.isValid())
        .map(roomSubscriptions -> toList(roomSubscriptions));
  }

  private List<Room> toList(RealmResults<RoomSubscription> roomSubscriptions) {
    int total = roomSubscriptions.size();

    final List<Room> roomList = new ArrayList<>(total);

    for (int i = 0; i < total; i++) {
      roomList.add(roomSubscriptions.get(i).asRoom());
    }

    return roomList;
  }
}
