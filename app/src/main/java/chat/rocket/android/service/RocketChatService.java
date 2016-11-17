package chat.rocket.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import bolts.Task;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.realm_helper.RealmStore;
import io.realm.RealmResults;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

  @Override public void onCreate() {
    super.onCreate();
    webSocketThreads = new HashMap<>();
    realmHelper = RealmStore.getDefault();
    connectionRequiredServerConfigObserver = realmHelper
            .createListObserver(realm -> realm.where(ServerConfig.class)
                .isNotNull("hostname")
                .equalTo("state", ServerConfig.STATE_READY)
                .findAll())
            .setOnUpdateListener(this::syncWebSocketThreadsWith);

    refreshServerConfigState();
  }

  private void refreshServerConfigState() {
    realmHelper.executeTransaction(realm -> {
      RealmResults<ServerConfig> configs = realm.where(ServerConfig.class).findAll();
      for (ServerConfig config: configs) {
        config.setState(ServerConfig.STATE_READY);
      }
      return null;
    }).continueWith(new LogcatIfError());;
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    realmHelper.executeTransaction(realm -> {
      RealmResults<ServerConfig> targetConfigs = realm
          .where(ServerConfig.class)
          .equalTo("state", ServerConfig.STATE_CONNECTION_ERROR)
          .isNotNull("session")
          .findAll();
      for (ServerConfig config : targetConfigs) {
        config.setState(ServerConfig.STATE_READY);
        config.setError(null);
      }
      return null;
    }).onSuccessTask(task -> {
      connectionRequiredServerConfigObserver.keepalive();
      return null;
    });
    return START_STICKY;
  }

  private void syncWebSocketThreadsWith(List<ServerConfig> configList) {
    final Iterator<Map.Entry<String, RocketChatWebSocketThread>> iterator =
        webSocketThreads.entrySet().iterator();

    while (iterator.hasNext()) {
      Map.Entry<String, RocketChatWebSocketThread> entry = iterator.next();
      String serverConfigId = entry.getKey();
      boolean found = false;
      for (ServerConfig config : configList) {
        if (serverConfigId.equals(config.getServerConfigId())) {
          found = true;
          break;
        }
      }
      if (!found) {
        RocketChatWebSocketThread thread = entry.getValue();
        if (thread != null) {
          RocketChatWebSocketThread.destroy(thread);
        }
        iterator.remove();
      }
    }

    for (ServerConfig config : configList) {
      findOrCreateWebSocketThread(config).onSuccess(task -> {
        RocketChatWebSocketThread thread = task.getResult();
        if (thread != null) {
          thread.keepalive();
        }
        return null;
      });
    }
  }

  private Task<RocketChatWebSocketThread> findOrCreateWebSocketThread(final ServerConfig config) {
    final String serverConfigId = config.getServerConfigId();
    if (webSocketThreads.containsKey(serverConfigId)) {
      return ServerConfig.updateState(serverConfigId, ServerConfig.STATE_CONNECTED)
          .onSuccessTask(_task -> Task.forResult(webSocketThreads.get(serverConfigId)));
    } else {
      return ServerConfig.updateState(serverConfigId, ServerConfig.STATE_CONNECTING)
          .onSuccessTask(_task -> {
            webSocketThreads.put(serverConfigId, null);
            return RocketChatWebSocketThread.getStarted(getApplicationContext(), config);
          })
          .onSuccessTask(task ->
              ServerConfig.updateState(serverConfigId, ServerConfig.STATE_CONNECTED)
                  .onSuccessTask(_task -> task))
          .onSuccessTask(task -> {
            webSocketThreads.put(serverConfigId, task.getResult());
            return task;
          });
    }
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
