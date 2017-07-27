package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Realm;

import chat.rocket.core.models.Session;
import chat.rocket.core.repositories.SessionRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class RealmSessionRepository extends RealmRepository implements SessionRepository {

  private final String hostname;

  public RealmSessionRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Flowable<Optional<Session>> getById(int id) {
    return Flowable.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(
            pair.first.where(RealmSession.class)
                .equalTo(RealmSession.ID, id)
                .findAll()
                .<RealmSession>asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it != null && it.isLoaded() && it.isValid())
        .map(realmSessions -> {
          if (realmSessions.size() == 0) {
            return Optional.absent();
          }
          return Optional.of(realmSessions.get(0).asSession());
        }));
  }

  @Override
  public Single<Boolean> save(Session session) {
    return Single.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null || looper == null) {
        return Single.just(false);
      }

      RealmSession realmSession = realm.where(RealmSession.class)
          .equalTo(RealmSession.ID, session.getSessionId())
          .findFirst();

      if (realmSession == null) {
        realmSession = new RealmSession();
      } else {
        realmSession = realm.copyFromRealm(realmSession);
      }

      realmSession.setSessionId(session.getSessionId());
      realmSession.setToken(session.getToken());
      realmSession.setTokenVerified(session.isTokenVerified());
      realmSession.setError(session.getError());

      realm.beginTransaction();

      return RxJavaInterop.toV2Flowable(realm.copyToRealmOrUpdate(realmSession)
          .asObservable())
          .filter(it -> it != null && it.isLoaded() && it.isValid())
          .firstElement()
          .doOnSuccess(it -> realm.commitTransaction())
          .doOnError(throwable -> realm.cancelTransaction())
          .doOnEvent((realmObject, throwable) -> close(realm, looper))
          .toSingle()
          .map(realmObject -> true);
    });
  }
}
