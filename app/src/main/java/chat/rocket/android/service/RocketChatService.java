package chat.rocket.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import chat.rocket.android.activity.MainActivity;
import chat.rocket.persistence.realm.RealmStore;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Single;

/**
 * Background service for Rocket.Chat.Application class.
 */
public class RocketChatService extends Service implements ConnectivityServiceInterface {

  private ConnectivityManagerInternal connectivityManager;
  private HashMap<String, RocketChatWebSocketThread> webSocketThreads;
  private Semaphore webSocketThreadLock = new Semaphore(1);

  public class LocalBinder extends Binder {
    ConnectivityServiceInterface getServiceInterface() {
      return RocketChatService.this;
    }
  }

  private final LocalBinder localBinder = new LocalBinder();

  /**
   * ensure RocketChatService alive.
   */
  /*package*/ static void keepAlive(Context context) {
    context.startService(new Intent(context, RocketChatService.class));
  }

  public static void bind(Context context, ServiceConnection serviceConnection) {
    context.bindService(
        new Intent(context, RocketChatService.class), serviceConnection, Context.BIND_AUTO_CREATE);
  }

  public static void unbind(Context context, ServiceConnection serviceConnection) {
    context.unbindService(serviceConnection);
  }

  @DebugLog
  @Override
  public void onCreate() {
    super.onCreate();
    connectivityManager = ConnectivityManager.getInstanceForInternal(getApplicationContext());
    connectivityManager.resetConnectivityStateList();
    webSocketThreads = new HashMap<>();
  }

  @DebugLog
  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    connectivityManager.ensureConnections();
    return START_NOT_STICKY;
  }

  @Override
  public Single<Boolean> ensureConnectionToServer(String hostname) { //called via binder.
    return getOrCreateWebSocketThread(hostname)
        .doOnError(err -> {
          webSocketThreads.remove(hostname);
          connectivityManager.notifyConnectionLost(hostname, ConnectivityManagerInternal.REASON_NETWORK_ERROR);
        })
        .flatMap(webSocketThreads -> webSocketThreads.keepAlive());
  }

  @Override
  public Single<Boolean> disconnectFromServer(String hostname) { //called via binder.
    return Single.defer(() -> {
      if (!webSocketThreads.containsKey(hostname)) {
        return Single.just(true);
      }

      RocketChatWebSocketThread thread = webSocketThreads.get(hostname);
      if (thread != null) {
        return thread.terminate()
            // after disconnection from server
            .doAfterTerminate(() -> {
              // remove RCWebSocket key from HashMap
              webSocketThreads.remove(hostname);
              // remove RealmConfiguration key from HashMap
              RealmStore.sStore.remove(hostname);
              // clear "cache" SharedPreference
              this.getSharedPreferences("cache", 0).edit().clear().apply();
              // start a fresh new MainActivity
              Intent intent = new Intent(this, MainActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
              this.startActivity(intent);
            });
      } else {
        return Observable.timer(1, TimeUnit.SECONDS).toSingle()
            .flatMap(_val -> disconnectFromServer(hostname));
      }
    });
  }

  @DebugLog
  private Single<RocketChatWebSocketThread> getOrCreateWebSocketThread(String hostname) {
    return Single.defer(() -> {
      webSocketThreadLock.acquire();
      if (webSocketThreads.containsKey(hostname)) {
        RocketChatWebSocketThread thread = webSocketThreads.get(hostname);
        webSocketThreadLock.release();
        return Single.just(thread);
      }
      return RocketChatWebSocketThread.getStarted(getApplicationContext(), hostname)
          .doOnSuccess(thread -> {
            webSocketThreads.put(hostname, thread);
            webSocketThreadLock.release();
          });
    });
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return localBinder;
  }
}
