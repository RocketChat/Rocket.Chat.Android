package chat.rocket.android.repositories;

import android.os.Looper;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.android.model.core.Room;
import chat.rocket.android.model.core.RoomHistoryState;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.android.repositories.core.RoomRepository;
import chat.rocket.persistence.realm.RealmStore;
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

      return realm.where(RoomSubscription.class)
          .equalTo(RoomSubscription.OPEN, true)
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

      return realm.where(RoomSubscription.class)
          .equalTo(RoomSubscription.ROOM_ID, roomId)
          .findFirst()
          .<RoomSubscription>asObservable()
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

  private List<Room> toList(RealmResults<RoomSubscription> roomSubscriptions) {
    int total = roomSubscriptions.size();

    final List<Room> roomList = new ArrayList<>(total);

    for (int i = 0; i < total; i++) {
      roomList.add(roomSubscriptions.get(i).asRoom());
    }

    return roomList;
  }
}
