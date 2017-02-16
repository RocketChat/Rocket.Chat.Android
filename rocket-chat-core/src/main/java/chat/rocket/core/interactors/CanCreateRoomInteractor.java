package chat.rocket.core.interactors;

import chat.rocket.core.repositories.UserRepository;
import rx.Observable;
import rx.Single;

public class CanCreateRoomInteractor {

  private final UserRepository userRepository;
  private final SessionInteractor sessionInteractor;

  public CanCreateRoomInteractor(UserRepository userRepository,
                                 SessionInteractor sessionInteractor) {
    this.userRepository = userRepository;
    this.sessionInteractor = sessionInteractor;
  }

  public Single<Boolean> canCreate(String roomId) {
    return Observable.zip(
        userRepository.getCurrent(),
        sessionInteractor.getDefault(),
        Observable.just(roomId),
        (user, session, room) -> user != null && session != null && room != null
    )
        .first()
        .toSingle();
  }
}
