package chat.rocket.android.repositories;

import io.realm.Realm;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.model.core.Room;
import chat.rocket.android.model.core.RoomHistoryState;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.persistence.realm.RealmStore;
import rx.Observable;

public class RealmRoomRepository implements RoomRepository {

  private final String hostname;

  public RealmRoomRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Observable<List<Room>> getOpenRooms() {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);

      if (realm == null) {
        return Observable.just(null);
      }

      return realm.where(RoomSubscription.class)
          .equalTo(RoomSubscription.OPEN, true)
          .findAll()
          .asObservable()
          .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
              && roomSubscriptions.isValid())
          .map(roomSubscriptions -> toList(roomSubscriptions));
    });
  }

  @Override
  public Observable<Room> getById(String roomId) {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);

      if (realm == null) {
        return Observable.just(null);
      }

      return realm.where(RoomSubscription.class)
          .equalTo(RoomSubscription.ROOM_ID, roomId)
          .findFirstAsync()
          .<RoomSubscription>asObservable()
          .filter(roomSubscription -> roomSubscription != null && roomSubscription.isLoaded()
              && roomSubscription.isValid())
          .map(roomSubscription -> roomSubscription.asRoom());
    });
  }

  @Override
  public Observable<RoomHistoryState> getHistoryStateByRoomId(String roomId) {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);

      if (realm == null) {
        return Observable.just(null);
      }

      return realm.where(LoadMessageProcedure.class)
          .equalTo(LoadMessageProcedure.ID, roomId)
          .findFirstAsync()
          .<LoadMessageProcedure>asObservable()
          .filter(loadMessageProcedure -> loadMessageProcedure != null
              && loadMessageProcedure.isLoaded() && loadMessageProcedure.isValid())
          .map(loadMessageProcedure -> loadMessageProcedure.asRoomHistoryState());
    });
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
