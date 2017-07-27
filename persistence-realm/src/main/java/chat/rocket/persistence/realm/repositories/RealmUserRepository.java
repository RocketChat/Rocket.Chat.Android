package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Case;
import io.realm.RealmResults;
import io.realm.Sort;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.core.SortDirection;
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

  @Override
  public Flowable<List<User>> getSortedLikeName(String name, SortDirection direction, int limit) {
    return Flowable.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(
            pair.first.where(RealmUser.class)
                .like(RealmUser.USERNAME, "*" + name + "*", Case.INSENSITIVE)
                .findAllSorted(RealmUser.USERNAME,
                    direction.equals(SortDirection.ASC) ? Sort.ASCENDING : Sort.DESCENDING)
                .asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(realmUsers -> realmUsers != null && realmUsers.isLoaded()
            && realmUsers.isValid())
        .map(realmUsers -> toList(safeSubList(realmUsers, 0, limit))));
  }

  private List<User> toList(RealmResults<RealmUser> realmUsers) {
    int total = realmUsers.size();

    final List<User> userList = new ArrayList<>(total);

    for (int i = 0; i < total; i++) {
      userList.add(realmUsers.get(i).asUser());
    }

    return userList;
  }

  private List<User> toList(List<RealmUser> realmUsers) {
    int total = realmUsers.size();

    final List<User> userList = new ArrayList<>(total);

    for (int i = 0; i < total; i++) {
      userList.add(realmUsers.get(i).asUser());
    }

    return userList;
  }
}
