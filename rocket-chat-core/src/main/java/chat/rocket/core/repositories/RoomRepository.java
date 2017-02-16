package chat.rocket.core.repositories;

import java.util.List;
import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomHistoryState;
import rx.Observable;
import rx.Single;

public interface RoomRepository {

  Observable<List<Room>> getAll();

  Observable<Room> getById(String roomId);

  Observable<RoomHistoryState> getHistoryStateByRoomId(String roomId);

  Single<Boolean> setHistoryState(RoomHistoryState roomHistoryState);
}
