package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.rocket.core.models.Session;
import io.reactivex.Flowable;
import io.reactivex.Single;

public interface SessionRepository {

  Flowable<Optional<Session>> getById(int id);

  Single<Boolean> save(Session session);
}
