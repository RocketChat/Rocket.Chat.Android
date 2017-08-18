package chat.rocket.persistence.realm.repositories;

import android.os.Looper;
import android.support.v4.util.Pair;
import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.realm.Case;
import io.realm.Sort;

import java.util.ArrayList;
import java.util.List;
import chat.rocket.core.SortDirection;
import chat.rocket.core.models.SpotlightRoom;
import chat.rocket.core.repositories.SpotlightRoomRepository;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import chat.rocket.persistence.realm.models.ddp.RealmSpotlightRoom;
import hu.akarnokd.rxjava.interop.RxJavaInterop;

public class RealmSpotlightRoomRepository extends RealmRepository implements SpotlightRoomRepository {

  private final String hostname;

  public RealmSpotlightRoomRepository(String hostname) {
    this.hostname = hostname;
  }

  @Override
  public Flowable<List<SpotlightRoom>> getSuggestionsFor(String name, SortDirection direction, int limit) {
    return Flowable.defer(() -> Flowable.using(
        () -> new Pair<>(RealmStore.getRealm(hostname), Looper.myLooper()),
        pair -> RxJavaInterop.toV2Flowable(
            pair.first.where(RealmSpotlightRoom.class)
                .like(RealmSpotlightRoom.Columns.NAME, "*" + name + "*", Case.INSENSITIVE)
                .beginGroup()
                .equalTo(RealmSpotlightRoom.Columns.TYPE, RealmRoom.TYPE_CHANNEL)
                .or()
                .equalTo(RealmSpotlightRoom.Columns.TYPE, RealmRoom.TYPE_PRIVATE)
                .endGroup()
                .findAllSorted(RealmSpotlightRoom.Columns.NAME, direction.equals(SortDirection.ASC) ? Sort.ASCENDING : Sort.DESCENDING)
                .asObservable()),
        pair -> close(pair.first, pair.second)
    )
        .unsubscribeOn(AndroidSchedulers.from(Looper.myLooper()))
        .filter(it -> it != null && it.isLoaded() && it.isValid())
        .map(realmSpotlightRooms -> toList(safeSubList(realmSpotlightRooms, 0, limit))));
  }

  private List<SpotlightRoom> toList(List<RealmSpotlightRoom> realmSpotlightRooms) {
    int total = realmSpotlightRooms.size();

    final List<SpotlightRoom> spotlightRooms = new ArrayList<>(total);

    for (int i = 0; i < total; i++) {
      spotlightRooms.add(realmSpotlightRooms.get(i).asSpotlightRoom());
    }

    return spotlightRooms;
  }
}