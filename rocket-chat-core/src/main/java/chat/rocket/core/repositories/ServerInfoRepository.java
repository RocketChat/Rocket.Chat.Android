package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import chat.rocket.core.models.ServerInfo;
import io.reactivex.Flowable;

public interface ServerInfoRepository {

  Flowable<Optional<ServerInfo>> getByHostname(String hostname);
}
