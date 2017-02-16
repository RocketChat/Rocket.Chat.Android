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
import java.util.concurrent.TimeUnit;
import chat.rocket.android.helper.RxHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.models.RealmBasedServerInfo;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Single;
import rx.subjects.PublishSubject;

/**
 * Connectivity management implementation.
 */
/*package*/ class RealmBasedConnectivityManager
    implements ConnectivityManagerApi, ConnectivityManagerInternal {
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


  /*package*/ RealmBasedConnectivityManager setContext(Context appContext) {
    this.appContext = appContext.getApplicationContext();
    return this;
  }

  @Override
  public void resetConnectivityStateList() {
    serverConnectivityList.clear();
    for (ServerInfo serverInfo : RealmBasedServerInfo.getServerInfoList()) {
      serverConnectivityList.put(serverInfo.getHostname(), ServerConnectivity.STATE_DISCONNECTED);
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
      connectToServerIfNeeded(hostname, true/* force connect */)
          .subscribe(_val -> {
          }, RCLog::e);
    }
  }

  @Override
  public void addOrUpdateServer(String hostname, @Nullable String name, boolean insecure) {
    RealmBasedServerInfo.addOrUpdate(hostname, name);
    RealmBasedServerInfo.setInsecure(hostname, insecure);
    if (!serverConnectivityList.containsKey(hostname)) {
      serverConnectivityList.put(hostname, ServerConnectivity.STATE_DISCONNECTED);
    }
    connectToServerIfNeeded(hostname, false)
        .subscribe(_val -> {
        }, RCLog::e);
  }

  @Override
  public void removeServer(String hostname) {
    RealmBasedServerInfo.remove(hostname);
    if (serverConnectivityList.containsKey(hostname)) {
      disconnectFromServerIfNeeded(hostname)
          .subscribe(_val -> {
          }, RCLog::e);
    }
  }

  @Override
  public Single<Boolean> connect(String hostname) {
    return connectToServerIfNeeded(hostname, false);
  }

  @Override
  public List<ServerInfo> getServerList() {
    return RealmBasedServerInfo.getServerInfoList();
  }

  @Override
  public ServerInfo getServerInfoForHost(String hostname) {
    return RealmBasedServerInfo.getServerInfoForHost(hostname);
  }

  private List<ServerConnectivity> getCurrentConnectivityList() {
    ArrayList<ServerConnectivity> list = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : serverConnectivityList.entrySet()) {
      list.add(new ServerConnectivity(entry.getKey(), entry.getValue()));
    }
    return list;
  }

  @DebugLog
  @Override
  public void notifyConnectionEstablished(String hostname, String session) {
    RealmBasedServerInfo.updateSession(hostname, session);
    serverConnectivityList.put(hostname, ServerConnectivity.STATE_CONNECTED);
    connectivitySubject.onNext(
        new ServerConnectivity(hostname, ServerConnectivity.STATE_CONNECTED));
  }

  @DebugLog
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

  private Single<Boolean> connectToServerIfNeeded(String hostname, boolean forceConnect) {
    return Single.defer(() -> {
      final int connectivity = serverConnectivityList.get(hostname);
      if (!forceConnect && connectivity == ServerConnectivity.STATE_CONNECTED) {
        return Single.just(true);
      }

      if (connectivity == ServerConnectivity.STATE_DISCONNECTING) {
        return waitForDisconnected(hostname)
            .flatMap(_val -> connectToServerIfNeeded(hostname, forceConnect));
      }

      if (connectivity == ServerConnectivity.STATE_CONNECTING) {
        return waitForConnected(hostname);
      }

      return connectToServer(hostname)
          //.doOnError(RCLog::e)
          .retryWhen(RxHelper.exponentialBackoff(3, 500, TimeUnit.MILLISECONDS));
    });
  }

  private Single<Boolean> disconnectFromServerIfNeeded(String hostname) {
    return Single.defer(() -> {
      final int connectivity = serverConnectivityList.get(hostname);
      if (connectivity == ServerConnectivity.STATE_DISCONNECTED) {
        return Single.just(true);
      }

      if (connectivity == ServerConnectivity.STATE_CONNECTING) {
        return waitForConnected(hostname)
            .onErrorReturn(err -> true)
            .flatMap(_val -> disconnectFromServerIfNeeded(hostname));
      }

      if (connectivity == ServerConnectivity.STATE_DISCONNECTING) {
        return waitForDisconnected(hostname);
      }

      return disconnectFromServer(hostname)
          //.doOnError(RCLog::e)
          .retryWhen(RxHelper.exponentialBackoff(3, 500, TimeUnit.MILLISECONDS));
    });
  }


  private Single<Boolean> waitForConnected(String hostname) {
    return connectivitySubject
        .filter(serverConnectivity -> hostname.equals(serverConnectivity.hostname))
        .map(serverConnectivity -> serverConnectivity.state)
        .filter(state ->
            state == ServerConnectivity.STATE_CONNECTED
                || state == ServerConnectivity.STATE_DISCONNECTED)
        .first()
        .toSingle()
        .flatMap(state ->
            state == ServerConnectivity.STATE_CONNECTED
                ? Single.just(true)
                : Single.error(new ServerConnectivity.DisconnectedException()));
  }

  private Single<Boolean> waitForDisconnected(String hostname) {
    return connectivitySubject
        .filter(serverConnectivity -> hostname.equals(serverConnectivity.hostname))
        .map(serverConnectivity -> serverConnectivity.state)
        .filter(state -> state == ServerConnectivity.STATE_DISCONNECTED)
        .first()
        .toSingle()
        .map(state -> true);
  }

  @DebugLog
  private Single<Boolean> connectToServer(String hostname) {
    return Single.defer(() -> {
      if (!serverConnectivityList.containsKey(hostname)) {
        return Single.error(new IllegalArgumentException("hostname not found"));
      }

      if (serverConnectivityList.get(hostname) != ServerConnectivity.STATE_CONNECTED) {
        // Mark as CONNECTING except for the case [forceConnect && connected] because
        // ensureConnectionToServer doesn't notify ConnectionEstablished/Lost is already connected.
        serverConnectivityList.put(hostname, ServerConnectivity.STATE_CONNECTING);
      }

      if (serviceInterface != null) {
        return serviceInterface.ensureConnectionToServer(hostname);
      } else {
        return Single.error(new IllegalStateException("not prepared"));
      }
    });
  }

  private Single<Boolean> disconnectFromServer(String hostname) {
    return Single.defer(() -> {
      if (!serverConnectivityList.containsKey(hostname)) {
        return Single.error(new IllegalArgumentException("hostname not found"));
      }
      serverConnectivityList.put(hostname, ServerConnectivity.STATE_DISCONNECTING);

      if (serviceInterface != null) {
        return serviceInterface.disconnectFromServer(hostname);
      } else {
        return Single.error(new IllegalStateException("not prepared"));
      }
    });
  }
}
