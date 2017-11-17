package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.rocket.core.models.Room;
import chat.rocket.core.models.RoomRole;
import chat.rocket.core.models.User;
import io.reactivex.Single;

public interface RoomRoleRepository {

  Single<Optional<RoomRole>> getFor(Room room, User user);
}
