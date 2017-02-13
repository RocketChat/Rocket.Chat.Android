package chat.rocket.core.interactors;

import chat.rocket.core.repositories.SessionRepository;
import chat.rocket.core.repositories.UserRepository;
import rx.Observable;
import rx.Single;

public class CanCreateRoomInteractor {

  private final UserRepository userRepository;
  private final SessionRepository sessionRepository;

  public CanCreateRoomInteractor(UserRepository userRepository,
                                 SessionRepository sessionRepository) {
    this.userRepository = userRepository;
    this.sessionRepository = sessionRepository;
  }

  public Single<Boolean> canCreate(String roomId) {
    return Observable.zip(
        userRepository.getCurrent(),
        sessionRepository.getDefault(),
        Observable.just(roomId),
        (user, session, room) -> user != null && session != null && room != null
    )
        .first()
        .toSingle();
  }
}
