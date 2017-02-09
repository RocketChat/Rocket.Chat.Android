package chat.rocket.android.repositories.core;

import java.util.List;
import chat.rocket.android.model.core.Room;
import chat.rocket.android.model.core.RoomHistoryState;
import rx.Observable;
import rx.Single;

public interface RoomRepository {

  Observable<List<Room>> getOpenRooms();

  Observable<Room> getById(String roomId);

  Observable<RoomHistoryState> getHistoryStateByRoomId(String roomId);

  Single<Boolean> setHistoryState(RoomHistoryState roomHistoryState);
}
