package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import java.util.ArrayList;
import java.util.List;

import chat.rocket.core.models.Message;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.MessageRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class RealmMessageRepository extends RealmRepository implements MessageRepository {

  private final String hostname;

  public RealmMessageRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Single<Optional<Message>> getById(String messageId) {
    return Single.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(
            pair.first.where(RealmMessage.class)
                .equalTo(RealmMessage.ID, messageId)
                .findAll()
                .<RealmResults<RealmMessage>>asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it.isLoaded() && it.isValid() && it.size() > 0)
        .map(realmMessages -> Optional.of(realmMessages.get(0).asMessage()))
        .first(Optional.absent()));
  }

  @Override
  public Single<Boolean> save(Message message) {
    return Single.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null || looper == null) {
        return Single.just(false);
      }

      RealmMessage realmMessage = realm.where(RealmMessage.class)
          .equalTo(RealmMessage.ID, message.getId())
          .findFirst();

      if (realmMessage == null) {
        realmMessage = new RealmMessage();
      } else {
        realmMessage = realm.copyFromRealm(realmMessage);
      }

      realmMessage.setId(message.getId());
      realmMessage.setSyncState(message.getSyncState());
      realmMessage.setTimestamp(message.getTimestamp());
      realmMessage.setRoomId(message.getRoomId());
      realmMessage.setMessage(message.getMessage());
      realmMessage.setEditedAt(message.getEditedAt());

      RealmUser realmUser = realmMessage.getUser();
      if (realmUser == null) {
        realmUser = realm.where(RealmUser.class)
            .equalTo(RealmUser.ID, message.getUser().getId())
            .findFirst();
      }
      realmMessage.setUser(realmUser);

      realm.beginTransaction();

      return RxJavaInterop.toV2Flowable(realm.copyToRealmOrUpdate(realmMessage)
          .asObservable())
          .filter(it -> it.isLoaded() && it.isValid())
          .firstElement()
          .doOnSuccess(it -> realm.commitTransaction())
          .doOnError(throwable -> realm.cancelTransaction())
          .doOnEvent((realmObject, throwable) -> close(realm, looper))
          .toSingle()
          .map(realmObject -> true);
    });
  }

  @Override
  public Single<Boolean> delete(Message message) {
    return Single.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null || looper == null) {
        return Single.just(false);
      }

      realm.beginTransaction();

      return RxJavaInterop.toV2Flowable(realm.where(RealmMessage.class)
          .equalTo(RealmMessage.ID, message.getId())
          .findAll()
          .<RealmResults<RealmMessage>>asObservable())
          .filter(realmObject -> realmObject.isLoaded() && realmObject.isValid())
          .firstElement()
          .toSingle()
          .flatMap(realmMessages -> Single.just(realmMessages.deleteAllFromRealm()))
          .doOnEvent((success, throwable) -> {
            if (success) {
              realm.commitTransaction();
            } else {
              realm.cancelTransaction();
            }
            close(realm, looper);
          });
    });
  }

  @Override
  public Flowable<List<Message>> getAllFrom(Room room) {
    return Flowable.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(pair.first.where(RealmMessage.class)
            .equalTo(RealmMessage.ROOM_ID, room.getRoomId())
            .isNotNull(RealmMessage.USER)
            .findAllSorted(RealmMessage.TIMESTAMP, Sort.DESCENDING)
            .asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it.isLoaded() && it.isValid())
        .map(this::toList)
        .distinctUntilChanged());
  }

  @Override
  public Single<Integer> unreadCountFor(Room room, User user) {
    return Single.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(pair.first.where(RealmMessage.class)
            .equalTo(RealmMessage.ROOM_ID, room.getId())
            .greaterThanOrEqualTo(RealmMessage.TIMESTAMP, room.getLastSeen())
            .notEqualTo(RealmMessage.USER_ID, user.getId())
            .findAll()
            .asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .map(RealmResults::size)
        .firstElement()
        .toSingle());
  }

  private List<Message> toList(RealmResults<RealmMessage> realmMessages) {
    final int total = realmMessages.size();
    final List<Message> messages = new ArrayList<>(total);

    for (int i = 0; i < total; i++) {
      messages.add(realmMessages.get(i).asMessage());
    }

    return messages;
  }
}
