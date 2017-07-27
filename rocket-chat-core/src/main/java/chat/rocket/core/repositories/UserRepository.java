package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;

import java.util.List;
import chat.rocket.core.SortDirection;
import chat.rocket.core.models.User;

public interface UserRepository {

  Flowable<Optional<User>> getCurrent();

  Flowable<List<User>> getSortedLikeName(String name, SortDirection direction, int limit);
}
