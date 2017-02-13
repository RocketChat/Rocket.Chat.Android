package chat.rocket.core.interactors;

import chat.rocket.core.models.Session;
import chat.rocket.core.repositories.SessionRepository;
import rx.Observable;

public class SessionInteractor {

  private static final int DEFAULT_ID = 0;

  private final SessionRepository sessionRepository;

  public SessionInteractor(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  public Observable<Session> getDefault() {
    return sessionRepository.getById(DEFAULT_ID);
  }

  public Observable<Session.State> getSessionState() {
    return getDefault()
        .map(this::getStateFrom);
  }

  private Session.State getStateFrom(Session session) {
    if (session == null) {
      return Session.State.UNAVAILABLE;
    }

    final String token = session.getToken();
    if (token == null || token.length() == 0) {
      return Session.State.UNAVAILABLE;
    }

    final String error = session.getError();
    if (error == null || error.length() == 0) {
      return Session.State.VALID;
    }

    return Session.State.INVALID;
  }
}
