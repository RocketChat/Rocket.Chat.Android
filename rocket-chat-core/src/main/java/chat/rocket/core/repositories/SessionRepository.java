package chat.rocket.core.repositories;

import chat.rocket.core.models.Session;
import rx.Observable;
import rx.Single;

public interface SessionRepository {

  Observable<Session> getById(int id);

  Single<Boolean> save(Session session);
}
