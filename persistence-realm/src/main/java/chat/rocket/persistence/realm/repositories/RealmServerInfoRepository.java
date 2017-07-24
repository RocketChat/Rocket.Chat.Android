package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import chat.rocket.core.models.ServerInfo;
import chat.rocket.core.repositories.ServerInfoRepository;
import chat.rocket.persistence.realm.models.RealmBasedServerInfo;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class RealmServerInfoRepository extends RealmRepository implements ServerInfoRepository {

  @Override
  public Flowable<Optional<ServerInfo>> getByHostname(String hostname) {
    return Flowable.defer(() -> Flowable.using(
        () -> new Pair<>(RealmBasedServerInfo.getRealm(), Looper.myLooper()),
        pair -> {
          RealmBasedServerInfo info = pair.first.where(RealmBasedServerInfo.class)
              .equalTo(RealmBasedServerInfo.ColumnName.HOSTNAME, hostname)
              .findFirst();

          if (info == null) {
            return Flowable.just(Optional.<RealmBasedServerInfo>absent());
          }

          return RxJavaInterop.toV2Flowable(info
              .<RealmBasedServerInfo>asObservable()
              .filter(it -> it.isLoaded() && it.isValid())
              .map(Optional::of));
        },
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .map(optional -> {
          if (optional.isPresent()) {
            return Optional.of(optional.get().getServerInfo());
          }

          return Optional.absent();
        }));
  }
}
