package chat.rocket.android.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import rx.Completable;
import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Connectivity management implementation.
 */
/*package*/ class ConnectivityManagerImpl implements ConnectivityManagerApi, ConnectivityManagerInternal {

  private final HashMap<String, Integer> serverConnectivityList = new HashMap<>();
  private final PublishSubject<ServerConnectivity> connectivitySubject = PublishSubject.create();
  private Context appContext;
  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder) {
      serviceInterface = ((RocketChatService.LocalBinder) binder).getServiceInterface();
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
      serviceInterface = null;
    }
  };
  private ConnectivityServiceInterface serviceInterface;


  /*package*/ ConnectivityManagerImpl setContext(Context appContext) {
    this.appContext = appContext;
    return this;
  }

  @Override
  public void resetConnectivityStateList() {
    serverConnectivityList.clear();
    for (ServerInfo serverInfo : ServerInfoImpl.getAllFromRealm()) {
      serverConnectivityList.put(serverInfo.hostname, ServerConnectivity.STATE_DISCONNECTED);
    }
  }

  @Override
  public void keepAliveServer() {
    RocketChatService.keepAlive(appContext);
    if (serviceInterface == null) {
      RocketChatService.bind(appContext, serviceConnection);
    }
  }

  @Override
  public void ensureConnections() {
    for (String hostname : serverConnectivityList.keySet()) {
      connectToServer(hostname); //force connect.
    }
  }

  @Override
  public void addOrUpdateServer(String hostname, @Nullable String name) {
    ServerInfoImpl.addOrUpdate(hostname, name);
    if (!serverConnectivityList.containsKey(hostname)) {
      serverConnectivityList.put(hostname, ServerConnectivity.STATE_DISCONNECTED);
    }
    connectToServerIfNeeded(hostname);
  }

  @Override
  public void removeServer(String hostname) {
    ServerInfoImpl.remove(hostname);
    if (serverConnectivityList.containsKey(hostname)) {
      disconnectFromServerIfNeeded(hostname);
    }
  }

  @Override
  public Completable connect(String hostname) {
    return connectToServerIfNeeded(hostname);
  }

  @Override
  public List<ServerInfo> getServerList() {
    return ServerInfoImpl.getAllFromRealm();
  }

  @Override
  public ServerInfo getServerInfoForHost(String hostname) {
    return ServerInfoImpl.getServerInfoForHost(hostname);
  }

  private List<ServerConnectivity> getCurrentConnectivityList() {
    ArrayList<ServerConnectivity> list = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : serverConnectivityList.entrySet()) {
      list.add(new ServerConnectivity(entry.getKey(), entry.getValue()));
    }
    return list;
  }

  @Override
  public void notifyConnectionEstablished(String hostname, String session) {
    ServerInfoImpl.updateSession(hostname, session);
    serverConnectivityList.put(hostname, ServerConnectivity.STATE_CONNECTED);
    connectivitySubject.onNext(
        new ServerConnectivity(hostname, ServerConnectivity.STATE_CONNECTED));
  }

  @Override
  public void notifyConnectionLost(String hostname, int reason) {
    serverConnectivityList.put(hostname, ServerConnectivity.STATE_DISCONNECTED);
    connectivitySubject.onNext(
        new ServerConnectivity(hostname, ServerConnectivity.STATE_DISCONNECTED));
  }

  @Override
  public Observable<ServerConnectivity> getServerConnectivityAsObservable() {
    return Observable.concat(Observable.from(getCurrentConnectivityList()), connectivitySubject);
  }

  private Completable connectToServerIfNeeded(String hostname) {
    final int connectivity = serverConnectivityList.get(hostname);
    if (connectivity == ServerConnectivity.STATE_CONNECTED) {
      return Completable.complete();
    }

    if (connectivity == ServerConnectivity.STATE_DISCONNECTING) {
      return waitForDisconnected(hostname).andThen(connectToServerIfNeeded(hostname));
    }

    if (connectivity == ServerConnectivity.STATE_CONNECTING) {
      return waitForConnected(hostname);
    }

    return connectToServer(hostname).retry(2);
  }

  private Completable disconnectFromServerIfNeeded(String hostname) {
    final int connectivity = serverConnectivityList.get(hostname);
    if (connectivity == ServerConnectivity.STATE_DISCONNECTED) {
      return Completable.complete();
    }

    if (connectivity == ServerConnectivity.STATE_CONNECTING) {
      return waitForConnected(hostname).andThen(disconnectFromServerIfNeeded(hostname));
    }

    if (connectivity == ServerConnectivity.STATE_DISCONNECTING) {
      return waitForDisconnected(hostname);
    }

    return disconnectFromServer(hostname).retry(2);
  }


  private Completable waitForConnected(String hostname) {
    return connectivitySubject
        .filter(serverConnectivity -> (hostname.equals(serverConnectivity.hostname)
            && serverConnectivity.state == ServerConnectivity.STATE_CONNECTED))
        .first()
        .toCompletable();
  }

  private Completable waitForDisconnected(String hostname) {
    return connectivitySubject
        .filter(serverConnectivity -> (hostname.equals(serverConnectivity.hostname)
            && serverConnectivity.state == ServerConnectivity.STATE_DISCONNECTED))
        .first()
        .toCompletable();
  }

  private Completable connectToServer(String hostname) {
    if (!serverConnectivityList.containsKey(hostname)) {
      throw new IllegalArgumentException("hostname not found");
    }
    serverConnectivityList.put(hostname, ServerConnectivity.STATE_CONNECTING);

    if (serviceInterface != null) {
      return serviceInterface.ensureConnectionToServer(hostname);
    } else {
      return Completable.error(new IllegalStateException("not prepared"));
    }
  }

  private Completable disconnectFromServer(String hostname) {
    if (!serverConnectivityList.containsKey(hostname)) {
      throw new IllegalArgumentException("hostname not found");
    }
    serverConnectivityList.put(hostname, ServerConnectivity.STATE_DISCONNECTING);

    if (serviceInterface != null) {
      return serviceInterface.disconnectFromServer(hostname);
    } else {
      return Completable.error(new IllegalStateException("not prepared"));
    }
  }
}
