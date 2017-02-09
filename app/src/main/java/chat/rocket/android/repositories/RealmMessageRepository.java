package chat.rocket.android.repositories;

import android.os.Looper;
import io.realm.Realm;
import io.realm.RealmResults;

import org.json.JSONObject;

import chat.rocket.android.model.core.Message;
import chat.rocket.android.model.core.Room;
import chat.rocket.android.model.ddp.RealmMessage;
import chat.rocket.android.model.ddp.RealmUser;
import chat.rocket.android.repositories.core.MessageRepository;
import chat.rocket.persistence.realm.RealmStore;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;

public class RealmMessageRepository extends RealmRepository implements MessageRepository {

  private final String hostname;

  public RealmMessageRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Single<Message> getById(String messageId) {
    return Single.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Single.just(null);
      }

      final RealmMessage realmMessage = realm.where(RealmMessage.class)
          .equalTo(RealmMessage.ID, messageId)
          .findFirst();

      if (realmMessage == null) {
        realm.close();
        return Single.just(null);
      }

      return realmMessage
          .<RealmMessage>asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(it -> it != null && it.isLoaded()
              && it.isValid())
          .first()
          .toSingle()
          .map(it -> it.asMessage());
    });
  }

  @Override
  public Single<Boolean> save(Message message) {
    return Single.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Single.just(false);
      }

      // need to improve this for real
      final JSONObject messageToSend = new JSONObject()
          .put(RealmMessage.ID, message.getId())
          .put(RealmMessage.SYNC_STATE, message.getSyncState())
          .put(RealmMessage.TIMESTAMP, message.getTimestamp())
          .put(RealmMessage.ROOM_ID, message.getRoomId())
          .put(RealmMessage.USER, new JSONObject()
              .put(RealmUser.ID, message.getUser().getId()))
          .put(RealmMessage.MESSAGE, message.getMessage());

      realm.beginTransaction();

      return realm.createOrUpdateObjectFromJson(RealmMessage.class, messageToSend)
          .asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(it -> it != null && it.isLoaded() && it.isValid())
          .first()
          .doOnNext(it -> realm.commitTransaction())
          .toSingle()
          .map(realmObject -> true);
    });
  }

  @Override
  public Single<Boolean> resend(Message message) {
    return Single.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Single.just(false);
      }

      final JSONObject messageToSend = new JSONObject()
          .put(RealmMessage.ID, message.getId())
          .put(RealmMessage.SYNC_STATE, message.getSyncState());

      realm.beginTransaction();

      return realm.createOrUpdateObjectFromJson(RealmMessage.class, messageToSend)
          .asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(it -> it != null && it.isLoaded() && it.isValid())
          .first()
          .doOnNext(it -> realm.commitTransaction())
          .toSingle()
          .map(realmObject -> true);
    });
  }

  @Override
  public Single<Boolean> delete(Message message) {
    return Single.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Single.just(false);
      }

      realm.beginTransaction();

      return realm.where(RealmMessage.class)
          .equalTo(RealmMessage.ID, message.getId())
          .findAll()
          .<RealmResults<RealmMessage>>asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(realmObject -> realmObject != null
              && realmObject.isLoaded() && realmObject.isValid())
          .first()
          .toSingle()
          .flatMap(realmMessages -> Single.just(realmMessages.deleteAllFromRealm()))
          .doOnEach(notification -> {
            if (notification.getValue()) {
              realm.commitTransaction();
            } else {
              realm.cancelTransaction();
            }
          });
    });
  }

  @Override
  public Observable<Message> getAllFrom(Room room) {
    return null;
  }

  @Override
  public Single<Integer> unreadCountFrom(Room room) {
    return null;
  }
}
