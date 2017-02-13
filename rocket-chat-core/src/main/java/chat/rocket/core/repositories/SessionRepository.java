package chat.rocket.core.repositories;

import chat.rocket.core.models.Session;
import rx.Observable;

public interface SessionRepository {

  Observable<Session> getById(int id);
}
