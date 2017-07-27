package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.RealmResults;

import chat.rocket.core.models.PublicSetting;
import chat.rocket.core.repositories.PublicSettingRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmPublicSetting;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class RealmPublicSettingRepository extends RealmRepository
    implements PublicSettingRepository {

  private final String hostname;

  public RealmPublicSettingRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Single<Optional<PublicSetting>> getById(String id) {
    return Single.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(
            pair.first.where(RealmPublicSetting.class)
                .equalTo(RealmPublicSetting.ID, id)
                .findAll()
                .<RealmResults<RealmPublicSetting>>asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it.isLoaded() && it.isValid() && it.size() > 0)
        .map(it -> Optional.of(it.get(0).asPublicSetting()))
        .first(Optional.absent()));
  }
}
