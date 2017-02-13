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
  public Observable<Session> getDefault() {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Observable.just(null);
      }

      final RealmSession realmSession =
          realm.where(RealmSession.class).equalTo(RealmSession.ID, RealmSession.DEFAULT_ID)
              .findFirst();

      if (realmSession == null) {
        realm.close();
        return Observable.just(null);
      }

      return realmSession
          .<RealmSession>asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(it -> it != null && it.isLoaded() && it.isValid())
          .map(RealmSession::asSession);
    });
  }
}
