package chat.rocket.core.interactors;

import com.fernandocejas.arrow.optional.Optional;
import io.reactivex.Flowable;
import io.reactivex.Single;

import chat.rocket.core.models.Session;
import chat.rocket.core.repositories.SessionRepository;

public class SessionInteractor {

  private static final int DEFAULT_ID = 0;

  private final SessionRepository sessionRepository;

  public SessionInteractor(SessionRepository sessionRepository) {
    this.sessionRepository = sessionRepository;
  }

  public Flowable<Optional<Session>> getDefault() {
    return sessionRepository.getById(DEFAULT_ID);
  }

  public Flowable<Session.State> getSessionState() {
    return getDefault()
        .map(sessionOptional -> getStateFrom(sessionOptional.orNull()));
  }

  public Single<Boolean> retryLogin() {
    return getDefault()
        .filter(Optional::isPresent)
        .map(Optional::get)
        .filter(session -> session.getToken() != null
            && (!session.isTokenVerified() || session.getError() != null))
        .map(session -> session.withTokenVerified(false).withError(null))
        .firstElement()
        .toSingle()
        .flatMap(sessionRepository::save);
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
