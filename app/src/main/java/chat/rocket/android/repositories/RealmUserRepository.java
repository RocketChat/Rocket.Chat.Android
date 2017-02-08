package chat.rocket.android.repositories;

import io.realm.Realm;

import chat.rocket.android.model.core.User;
import chat.rocket.android.model.ddp.RealmUser;
import rx.Observable;

public class RealmUserRepository implements UserRepository {

  private final Realm realm;

  public RealmUserRepository(Realm realm) {
    this.realm = realm;
  }

  @Override
  public Observable<User> getCurrentUser() {
    return realm.where(RealmUser.class)
        .isNotEmpty(RealmUser.EMAILS)
        .findFirstAsync()
        .<RealmUser>asObservable()
        .filter(realmUser -> realmUser != null && realmUser.isLoaded() && realmUser.isValid())
        .map(realmUser -> realmUser.asUser());
  }
}
