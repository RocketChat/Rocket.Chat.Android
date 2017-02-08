package chat.rocket.android.repositories;

import java.util.List;
import chat.rocket.android.model.core.Room;
import rx.Observable;

public interface RoomRepository {

  Observable<List<Room>> getOpenRooms();
}
