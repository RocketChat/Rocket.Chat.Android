package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;

import chat.rocket.core.models.Session;

public interface SessionRepository {

  Flowable<Optional<Session>> getById(int id);

  Single<Boolean> save(Session session);
}
