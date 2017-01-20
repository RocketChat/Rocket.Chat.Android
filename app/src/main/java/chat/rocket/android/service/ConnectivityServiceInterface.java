package chat.rocket.android.service;

import rx.Completable;

public interface ConnectivityServiceInterface {
  Completable ensureConnectionToServer(String hostname);

  Completable disconnectFromServer(String hostname);
}
