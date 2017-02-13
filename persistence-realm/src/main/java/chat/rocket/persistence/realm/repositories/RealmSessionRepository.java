package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import io.realm.Realm;

import chat.rocket.core.models.Session;
import chat.rocket.core.repositories.SessionRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class RealmSessionRepository extends RealmRepository implements SessionRepository {

  private final String hostname;

  public RealmSessionRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Observable<Session> getById(int id) {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null || looper == null) {
        return Observable.just(null);
      }

      return realm.where(RealmSession.class)
          .equalTo(RealmSession.ID, id)
          .findAll()
          .<RealmSession>asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(it -> it != null && it.isLoaded() && it.isValid())
          .map(realmSessions -> {
            if (realmSessions.size() == 0) {
              return null;
            }
            return realmSessions.get(0).asSession();
          });
    });
  }
}
