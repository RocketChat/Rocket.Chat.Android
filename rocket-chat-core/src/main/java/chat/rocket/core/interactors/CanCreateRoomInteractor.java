package chat.rocket.core.interactors;

import io.reactivex.Flowable;
import io.reactivex.Single;

import chat.rocket.core.repositories.UserRepository;

public class CanCreateRoomInteractor {

  private final UserRepository userRepository;
  private final SessionInteractor sessionInteractor;

  public CanCreateRoomInteractor(UserRepository userRepository,
                                 SessionInteractor sessionInteractor) {
    this.userRepository = userRepository;
    this.sessionInteractor = sessionInteractor;
  }

  public Single<Boolean> canCreate(String roomId) {
    return Flowable.zip(
        userRepository.getCurrent(),
        sessionInteractor.getDefault(),
        Flowable.just(roomId),
        (user, session, room) -> user != null && session != null && room != null
    )
        .first(false);
  }
}
