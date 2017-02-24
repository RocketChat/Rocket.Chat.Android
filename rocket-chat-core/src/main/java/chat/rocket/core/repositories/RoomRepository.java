package chat.rocket.core.repositories;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;

import java.util.List;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomHistoryState;

public interface RoomRepository {

  Flowable<List<Room>> getAll();

  Flowable<Room> getById(String roomId);

  Flowable<Optional<RoomHistoryState>> getHistoryStateByRoomId(String roomId);

  Single<Boolean> setHistoryState(RoomHistoryState roomHistoryState);
}
