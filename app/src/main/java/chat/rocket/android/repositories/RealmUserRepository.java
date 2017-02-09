package chat.rocket.android.repositories;

import io.realm.Realm;

import chat.rocket.android.model.core.User;
import chat.rocket.android.model.ddp.RealmUser;
import chat.rocket.persistence.realm.RealmStore;
import rx.Observable;

public class RealmUserRepository implements UserRepository {

  private final String hostname;

  public RealmUserRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Observable<User> getCurrentUser() {
    return Observable.defer(() -> {
      final Realm realm = RealmStore.getRealm(hostname);

      if (realm == null) {
        return Observable.just(null);
      }

      final RealmUser realmUser = realm.where(RealmUser.class)
          .isNotEmpty(RealmUser.EMAILS)
          .findFirst();

      if (realmUser == null) {
        return Observable.just(null);
      }

      return realmUser
          .<RealmUser>asObservable()
          .filter(it -> it != null && it.isLoaded() && it.isValid())
          .map(it -> it.asUser());
    });
  }
}
