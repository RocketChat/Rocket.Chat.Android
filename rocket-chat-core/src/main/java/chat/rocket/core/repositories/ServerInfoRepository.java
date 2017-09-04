package chat.rocket.core.repositories;

import com.hadisatrio.optional.Optional;

import io.reactivex.Flowable;

import chat.rocket.core.models.ServerInfo;

public interface ServerInfoRepository {

  Flowable<Optional<ServerInfo>> getByHostname(String hostname);
}
