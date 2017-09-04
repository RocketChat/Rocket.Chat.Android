package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.List;
import chat.rocket.core.SortDirection;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomHistoryState;

public interface RoomRepository {

  Flowable<List<Room>> getAll();

  Flowable<Optional<Room>> getById(String roomId);

  Flowable<Optional<RoomHistoryState>> getHistoryStateByRoomId(String roomId);

  Single<Boolean> setHistoryState(RoomHistoryState roomHistoryState);

  Flowable<List<Room>> getSortedLikeName(String name, SortDirection direction, int limit);

  Flowable<List<Room>> getLatestSeen(int limit);
}
