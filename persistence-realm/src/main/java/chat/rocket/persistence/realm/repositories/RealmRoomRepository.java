package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomHistoryState;
import chat.rocket.core.repositories.RoomRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import chat.rocket.persistence.realm.models.internal.LoadMessageProcedure;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

public class RealmRoomRepository extends RealmRepository implements RoomRepository {

  private final String hostname;

  public RealmRoomRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Observable<List<Room>> getOpenRooms() {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Observable.just(null);
      }

      return realm.where(RealmRoom.class)
          .equalTo(RealmRoom.OPEN, true)
          .findAll()
          .asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(roomSubscriptions -> roomSubscriptions != null && roomSubscriptions.isLoaded()
              && roomSubscriptions.isValid())
          .map(roomSubscriptions -> toList(roomSubscriptions));
    });
  }

  @Override
  public Observable<Room> getById(String roomId) {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Observable.just(null);
      }

      return realm.where(RealmRoom.class)
          .equalTo(RealmRoom.ROOM_ID, roomId)
          .findFirst()
          .<RealmRoom>asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(roomSubscription -> roomSubscription != null && roomSubscription.isLoaded()
              && roomSubscription.isValid())
          .map(roomSubscription -> roomSubscription.asRoom());
    });
  }

  @Override
  public Observable<RoomHistoryState> getHistoryStateByRoomId(String roomId) {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Observable.just(null);
      }

      return realm.where(LoadMessageProcedure.class)
          .equalTo(LoadMessageProcedure.ID, roomId)
          .findFirst()
          .<LoadMessageProcedure>asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(loadMessageProcedure -> loadMessageProcedure != null
              && loadMessageProcedure.isLoaded() && loadMessageProcedure.isValid())
          .map(loadMessageProcedure -> loadMessageProcedure.asRoomHistoryState());
    });
  }

  @Override
  public Single<Boolean> setHistoryState(RoomHistoryState roomHistoryState) {
    return Single.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Single.just(false);
      }

      LoadMessageProcedure loadMessage = new LoadMessageProcedure();
      loadMessage.setRoomId(roomHistoryState.getRoomId());
      loadMessage.setSyncState(roomHistoryState.getSyncState());
      loadMessage.setCount(roomHistoryState.getCount());
      loadMessage.setReset(roomHistoryState.isReset());
      loadMessage.setHasNext(!roomHistoryState.isComplete());
      loadMessage.setTimestamp(roomHistoryState.getTimestamp());

      realm.beginTransaction();

      return realm.copyToRealmOrUpdate(loadMessage)
          .asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(realmObject -> realmObject != null
              && realmObject.isLoaded() && realmObject.isValid())
          .first()
          .doOnNext(realmObject -> realm.commitTransaction())
          .toSingle()
          .map(realmObject -> true);
    });
  }

  private List<Room> toList(RealmResults<RealmRoom> realmRooms) {
    int total = realmRooms.size();

    final List<Room> roomList = new ArrayList<>(total);

    for (int i = 0; i < total; i++) {
      roomList.add(realmRooms.get(i).asRoom());
    }

    return roomList;
  }
}
