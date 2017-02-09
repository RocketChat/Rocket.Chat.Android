package chat.rocket.android.repositories;

import java.util.List;
import chat.rocket.android.model.core.Room;
import chat.rocket.android.model.core.RoomHistoryState;
import rx.Observable;

public interface RoomRepository {

  Observable<List<Room>> getOpenRooms();

  Observable<Room> getById(String roomId);

  Observable<RoomHistoryState> getHistoryStateByRoomId(String roomId);
}
