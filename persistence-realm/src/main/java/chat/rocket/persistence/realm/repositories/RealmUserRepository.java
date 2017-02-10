package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import io.realm.Realm;

import chat.rocket.core.models.User;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class RealmUserRepository extends RealmRepository implements UserRepository {

  private final String hostname;

  public RealmUserRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Observable<User> getCurrentUser() {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);
      final Looper looper = Looper.myLooper();

      if (realm == null) {
        return Observable.just(null);
      }

      final RealmUser realmUser = realm.where(RealmUser.class)
          .isNotEmpty(RealmUser.EMAILS)
          .findFirst();

      if (realmUser == null) {
        realm.close();
        return Observable.just(null);
      }

      return realmUser
          .<RealmUser>asObservable()
          .unsubscribeOn(AndroidSchedulers.from(looper))
          .doOnUnsubscribe(() -> close(realm, looper))
          .filter(it -> it != null && it.isLoaded() && it.isValid())
          .map(it -> it.asUser());
    });
  }
}
