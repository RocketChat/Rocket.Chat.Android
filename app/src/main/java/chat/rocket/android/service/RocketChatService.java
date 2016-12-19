package chat.rocket.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import io.realm.RealmResults;

import java.util.HashMap;
import java.util.List;
import bolts.Task;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.realm_helper.RealmStore;

/**
 * Background service for Rocket.Chat.Application class.
 */
public class RocketChatService extends Service {

  private RealmHelper realmHelper;
  private HashMap<String, RocketChatWebSocketThread> webSocketThreads;
  private RealmListObserver<ServerConfig> connectionRequiredServerConfigObserver;

  /**
   * ensure RocketChatService alive.
   */
  public static void keepalive(Context context) {
    context.startService(new Intent(context, RocketChatService.class));
  }

  @Override
  public void onCreate() {
    super.onCreate();
    webSocketThreads = new HashMap<>();
    realmHelper = RealmStore.getDefault();
    connectionRequiredServerConfigObserver = realmHelper
        .createListObserver(realm -> realm.where(ServerConfig.class)
            .isNotNull("hostname")
            .equalTo("state", ServerConfig.STATE_READY)
            .findAll())
        .setOnUpdateListener(this::connectToServerWithServerConfig);

    refreshServerConfigState();
  }

  private void refreshServerConfigState() {
    realmHelper.executeTransaction(realm -> {
      RealmResults<ServerConfig> configs = realm.where(ServerConfig.class)
          .notEqualTo("state", ServerConfig.STATE_READY)
          .findAll();
      for (ServerConfig config : configs) {
        config.setState(ServerConfig.STATE_READY);
      }
      return null;
    }).continueWith(new LogcatIfError());
    ;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    List<ServerConfig> configs = realmHelper.executeTransactionForReadResults(realm ->
        realm.where(ServerConfig.class)
            .equalTo("state", ServerConfig.STATE_CONNECTED)
            .findAll());
    for (ServerConfig config : configs) {
      String serverConfigId = config.getServerConfigId();
      if (webSocketThreads.containsKey(serverConfigId)) {
        RocketChatWebSocketThread thread = webSocketThreads.get(serverConfigId);
        if (thread != null) {
          thread.keepAlive();
        }
      }
    }

    realmHelper.executeTransaction(realm -> {
      RealmResults<ServerConfig> targetConfigs = realm
          .where(ServerConfig.class)
          .beginGroup()
          .equalTo("state", ServerConfig.STATE_CONNECTION_ERROR)
          .or()
          .isNotNull("error")
          .endGroup()
          .isNotNull("session")
          .findAll();
      for (ServerConfig config : targetConfigs) {
        config.setState(ServerConfig.STATE_READY);
        config.setError(null);
      }
      return null;
    }).onSuccessTask(task -> {
      connectionRequiredServerConfigObserver.sub();
      return null;
    });
    return START_STICKY;
  }

  private void connectToServerWithServerConfig(List<ServerConfig> configList) {
    if (configList.isEmpty()) {
      return;
    }

    ServerConfig config = configList.get(0);
    final String serverConfigId = config.getServerConfigId();
    ServerConfig.updateState(serverConfigId, ServerConfig.STATE_CONNECTING)
        .onSuccessTask(task -> createWebSocketThread(config))
        .onSuccessTask(task -> {
          RocketChatWebSocketThread thread = task.getResult();
          if (thread != null) {
            thread.keepAlive();
          }
          return ServerConfig.updateState(serverConfigId, ServerConfig.STATE_CONNECTED);
        }).continueWith(new LogcatIfError());
  }

  private Task<RocketChatWebSocketThread> createWebSocketThread(final ServerConfig config) {
    final String serverConfigId = config.getServerConfigId();
    webSocketThreads.put(serverConfigId, null);
    return RocketChatWebSocketThread.getStarted(getApplicationContext(), config)
        .onSuccessTask(task -> {
          webSocketThreads.put(serverConfigId, task.getResult());
          return task;
        });
  }

  @Override
  public void onDestroy() {
    if (connectionRequiredServerConfigObserver != null) {
      connectionRequiredServerConfigObserver.unsub();
    }
    super.onDestroy();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
