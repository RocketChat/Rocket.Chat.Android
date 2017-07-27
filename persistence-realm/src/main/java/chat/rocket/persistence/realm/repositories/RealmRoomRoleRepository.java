package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.RealmResults;

import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomRole;
import chat.rocket.core.models.User;
import chat.rocket.core.repositories.RoomRoleRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmRoomRole;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class RealmRoomRoleRepository extends RealmRepository implements RoomRoleRepository {

  private final String hostname;

  public RealmRoomRoleRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Single<Optional<RoomRole>> getFor(Room room, User user) {
    return Single.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(
            pair.first.where(RealmRoomRole.class)
                .equalTo(RealmRoomRole.Columns.ROOM_ID, room.getId())
                .equalTo(RealmRoomRole.Columns.USER + "." + RealmUser.ID, user.getId())
                .findAll()
                .<RealmResults<RealmRoomRole>>asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it.isLoaded() && it.isValid())
        .map(it -> {
          if (it.size() == 0) {
            return Optional.<RoomRole>absent();
          }
          return Optional.of(it.get(0).asRoomRole());
        })
        .first(Optional.absent()));
  }
}
