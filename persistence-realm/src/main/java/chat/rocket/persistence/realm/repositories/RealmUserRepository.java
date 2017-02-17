package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.RealmResults;

import chat.rocket.core.models.User;
import chat.rocket.core.repositories.UserRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class RealmUserRepository extends RealmRepository implements UserRepository {

  private final String hostname;

  public RealmUserRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Flowable<Optional<User>> getCurrent() {
    return Flowable.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(
            pair.first.where(RealmUser.class)
                .isNotEmpty(RealmUser.EMAILS)
                .findAll()
                .<RealmResults<RealmUser>>asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it != null && it.isLoaded()
            && it.isValid())
        .map(realmUsers -> {
          if (realmUsers.size() > 0) {
            return Optional.of(realmUsers.get(0).asUser());
          }

          return Optional.<User>absent();
        }));
  }
}
