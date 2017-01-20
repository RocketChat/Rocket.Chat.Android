package chat.rocket.android.service;

import android.support.annotation.Nullable;

import java.util.List;
import rx.Completable;
import rx.Observable;

/**
 * interfaces used for Activity/Fragment and other UI-related logic.
 */
public interface ConnectivityManagerApi {
  void keepAliveServer();

  void addOrUpdateServer(String hostname, @Nullable String name);

  void removeServer(String hostname);

  Completable connect(String hostname);

  List<ServerInfo> getServerList();

  Observable<ServerConnectivity> getServerConnectivityAsObservable();
}
