package chat.rocket.android.helper;

import com.hadisatrio.optional.Optional;

import chat.rocket.android.fragment.chatroom.RocketChatAbsoluteUrl;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.repositories.ServerInfoRepository;
import chat.rocket.core.repositories.UserRepository;
import io.reactivex.Flowable;
import io.reactivex.Single;

public class AbsoluteUrlHelper {

  private final String hostname;
  private final ServerInfoRepository serverInfoRepository;
  private final UserRepository userRepository;
  private final SessionInteractor sessionInteractor;

  public AbsoluteUrlHelper(String hostname,
                           ServerInfoRepository serverInfoRepository,
                           UserRepository userRepository,
                           SessionInteractor sessionInteractor) {
    this.hostname = hostname;
    this.serverInfoRepository = serverInfoRepository;
    this.userRepository = userRepository;
    this.sessionInteractor = sessionInteractor;
  }

  public Single<Optional<RocketChatAbsoluteUrl>> getRocketChatAbsoluteUrl() {
    return Flowable.zip(
        serverInfoRepository.getByHostname(hostname)
            .filter(Optional::isPresent)
            .map(Optional::get),
        userRepository.getCurrent()
            .filter(Optional::isPresent)
            .map(Optional::get),
        sessionInteractor.getDefault()
            .filter(Optional::isPresent)
            .map(Optional::get),
        (info, user, session) -> Optional.of(new RocketChatAbsoluteUrl(
            info, user, session
        ))
    )
        .first(Optional.absent());
  }
}
