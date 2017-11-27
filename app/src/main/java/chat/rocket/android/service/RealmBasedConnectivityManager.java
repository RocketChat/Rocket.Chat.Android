package chat.rocket.android.service;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.RxHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.models.RealmBasedServerInfo;
import hugo.weaving.DebugLog;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

/**
 * Connectivity management implementation.
 */
/*package*/ class RealmBasedConnectivityManager
        implements ConnectivityManagerApi, ConnectivityManagerInternal {
    private volatile ConcurrentHashMap<String, Integer> serverConnectivityList = new ConcurrentHashMap<>();
    private volatile BehaviorSubject<ServerConnectivity> connectivitySubject = BehaviorSubject.createDefault(ServerConnectivity.CONNECTED);
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

    @SuppressLint("RxLeakedSubscription")
    @DebugLog
    @Override
    public void ensureConnections() {
        String hostname = new RocketChatCache(appContext).getSelectedServerHostname();
        if (hostname == null) {
            return;
        }
        connectToServerIfNeeded(hostname, true/* force connect */)
                .subscribeOn(Schedulers.io())
                .subscribe(connected -> {
                    if (!connected) {
                        notifyConnectionLost(hostname, DDPClient.REASON_NETWORK_ERROR);
                    }
                }, error -> {
                    RCLog.e(error);
                    notifyConnectionLost(hostname, DDPClient.REASON_NETWORK_ERROR);
                });
    }

    @SuppressLint("RxLeakedSubscription")
    @Override
    public void addOrUpdateServer(String hostname, @Nullable String name, boolean insecure) {
        RealmBasedServerInfo.addOrUpdate(hostname, name, insecure);
        if (!serverConnectivityList.containsKey(hostname)) {
            serverConnectivityList.put(hostname, ServerConnectivity.STATE_DISCONNECTED);
        }
        connectToServerIfNeeded(hostname, false)
                .subscribe(connected -> {
                }, RCLog::e);
    }

    @SuppressLint("RxLeakedSubscription")
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
        if (session != null) {
            RealmBasedServerInfo.updateSession(hostname, session);
        }
        serverConnectivityList.put(hostname, ServerConnectivity.STATE_CONNECTED);
        connectivitySubject.onNext(
                new ServerConnectivity(hostname, ServerConnectivity.STATE_CONNECTED));
    }

    @DebugLog
    @Override
    public void notifyConnectionLost(String hostname, int code) {
        serverConnectivityList.put(hostname, ServerConnectivity.STATE_DISCONNECTED);
        connectivitySubject.onNext(
                new ServerConnectivity(hostname, ServerConnectivity.STATE_DISCONNECTED, code));
    }

    @DebugLog
    @Override
    public void notifyConnecting(String hostname) {
        serverConnectivityList.put(hostname, ServerConnectivity.STATE_CONNECTING);
        connectivitySubject.onNext(
                new ServerConnectivity(hostname, ServerConnectivity.STATE_CONNECTING));
    }

    @Override
    public Flowable<ServerConnectivity> getServerConnectivityAsObservable() {
        return connectivitySubject.toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public int getConnectivityState(@NonNull String hostname) {
        return serverConnectivityList.get(hostname);
    }

    @DebugLog
    private Single<Boolean> connectToServerIfNeeded(String hostname, boolean forceConnect) {
        return Single.defer(() -> {
            Integer state = serverConnectivityList.get(hostname);
            if (state == null) {
                state = ServerConnectivity.STATE_DISCONNECTED;
            }
            final int connectivity = state;
            if (!forceConnect && connectivity == ServerConnectivity.STATE_CONNECTED) {
                return Single.just(true);
            }

            if (connectivity == ServerConnectivity.STATE_DISCONNECTING) {
                return waitForDisconnected(hostname)
                        .flatMap(_val -> connectToServerIfNeeded(hostname, forceConnect));
            }

            if (connectivity == ServerConnectivity.STATE_DISCONNECTED) {
//              notifyConnecting(hostname);
            }

            return connectToServer(hostname)
                    .retry(exception -> exception instanceof ThreadLooperNotPreparedException)
                    .onErrorResumeNext(Single.just(false));
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
                        .doOnError(err -> notifyConnectionLost(hostname, DDPClient.REASON_NETWORK_ERROR))
                        .flatMap(_val -> disconnectFromServerIfNeeded(hostname));
            }

            if (connectivity == ServerConnectivity.STATE_DISCONNECTING) {
                return waitForDisconnected(hostname);
            }

            return disconnectFromServer(hostname)
                    .retryWhen(RxHelper.exponentialBackoff(1, 500, TimeUnit.MILLISECONDS));
        });
    }

    @DebugLog
    private Single<Boolean> waitForConnected(String hostname) {
        return connectivitySubject
                .filter(serverConnectivity -> hostname.equals(serverConnectivity.hostname))
                .map(serverConnectivity -> serverConnectivity.state)
                .filter(state ->
                        state == ServerConnectivity.STATE_CONNECTED
                                || state == ServerConnectivity.STATE_DISCONNECTED)
                .firstElement()
                .toSingle()
                .flatMap(state ->
                        state == ServerConnectivity.STATE_CONNECTED
                                ? Single.just(true)
                                : Single.error(new ServerConnectivity.DisconnectedException()));
    }

    @DebugLog
    private Single<Boolean> waitForDisconnected(String hostname) {
        return connectivitySubject
                .filter(serverConnectivity -> hostname.equals(serverConnectivity.hostname))
                .map(serverConnectivity -> serverConnectivity.state)
                .filter(state -> state == ServerConnectivity.STATE_DISCONNECTED)
                .firstElement()
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
//                serverConnectivityList.put(hostname, ServerConnectivity.STATE_CONNECTING);
            }

            if (serviceInterface != null) {
                return serviceInterface.ensureConnectionToServer(hostname);
            } else {
                return Single.error(new ThreadLooperNotPreparedException("not prepared"));
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
                return serviceInterface.disconnectFromServer(hostname)
                        // //after disconnection from server, remove HOSTNAME key from HashMap
                        .doAfterTerminate(() -> serverConnectivityList.remove(hostname));
            } else {
                return Single.error(new IllegalStateException("not prepared"));
            }
        });
    }

    private static class ThreadLooperNotPreparedException extends IllegalStateException {
        ThreadLooperNotPreparedException(String message) {
            super(message);
        }
    }
}