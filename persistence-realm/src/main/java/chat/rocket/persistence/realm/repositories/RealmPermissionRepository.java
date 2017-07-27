package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.RealmResults;

import chat.rocket.core.models.Permission;
import chat.rocket.core.repositories.PermissionRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmPermission;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class RealmPermissionRepository extends RealmRepository implements PermissionRepository {

  private final String hostname;

  public RealmPermissionRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Single<Optional<Permission>> getById(String id) {
    return Single.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(
            pair.first.where(RealmPermission.class)
                .equalTo(RealmPermission.Columns.ID, id)
                .findAll()
                .<RealmResults<RealmPermission>>asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it.isLoaded() && it.isValid())
        .map(it -> {
          if (it.size() == 0) {
            return Optional.<Permission>absent();
          }
          return Optional.of(it.get(0).asPermission());
        })
        .first(Optional.absent()));
  }
}
