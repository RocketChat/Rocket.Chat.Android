package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;

import com.hadisatrio.optional.Optional;

import chat.rocket.core.models.ServerInfo;
import chat.rocket.core.repositories.ServerInfoRepository;
import chat.rocket.persistence.realm.models.RealmBasedServerInfo;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;

public class RealmServerInfoRepository extends RealmRepository implements ServerInfoRepository {

  @Override
  public Flowable<Optional<ServerInfo>> getByHostname(String hostname) {
    return Flowable.defer(() -> Flowable.using(
        () -> new Pair<>(RealmBasedServerInfo.getServerRealm(), Looper.myLooper()),
        pair -> {
          RealmBasedServerInfo info = pair.first.where(RealmBasedServerInfo.class)
              .equalTo(RealmBasedServerInfo.ColumnName.HOSTNAME, hostname)
              .findFirst();

          if (info == null) {
            return Flowable.just(Optional.<RealmBasedServerInfo>absent());
          }

          return info.<RealmBasedServerInfo>asFlowable()
              .filter(it -> it.isLoaded() && it.isValid())
              .map(Optional::of);
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
