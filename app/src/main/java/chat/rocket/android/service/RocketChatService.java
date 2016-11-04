package chat.rocket.android.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import bolts.Task;
import chat.rocket.android.model.ServerConfig;
import io.realm.Realm;
import io.realm.RealmResults;
import jp.co.crowdworks.realm_java_helpers.RealmListObserver;

/**
 * Background service for Rocket.Chat.Application class.
 */
public class RocketChatService extends Service {

    /**
     * ensure RocketChatService alive.
     */
    public static void keepalive(Context context) {
        context.startService(new Intent(context, RocketChatService.class));
    }

    /**
     * force stop RocketChatService.
     */
    public static void kill(Context context) {
        context.stopService(new Intent(context, RocketChatService.class));
    }

    private HashMap<String, RocketChatWebSocketThread> mWebSocketThreads;
    private RealmListObserver<ServerConfig> mConnectionRequiredServerConfigObserver =
            new RealmListObserver<ServerConfig>() {
                @Override
                protected RealmResults<ServerConfig> queryItems(Realm realm) {
                    return realm.where(ServerConfig.class)
                            .isNotNull("hostname")
                            .isNull("connectionError")
                            .findAll();
                }

                @Override
                protected void onCollectionChanged(List<ServerConfig> list) {
                    syncWebSocketThreadsWith(list);
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();
        mWebSocketThreads = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mConnectionRequiredServerConfigObserver.keepalive();
        return START_STICKY;
    }

    private void syncWebSocketThreadsWith(List<ServerConfig> configList) {
        final Iterator<Map.Entry<String, RocketChatWebSocketThread>> iterator =
                mWebSocketThreads.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, RocketChatWebSocketThread> entry = iterator.next();
            String serverConfigId = entry.getKey();
            boolean found = false;
            for (ServerConfig config: configList) {
                if (serverConfigId.equals(config.getId())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                RocketChatWebSocketThread.terminate(entry.getValue());
                iterator.remove();
            }
        }

        for (ServerConfig config: configList) {
            findOrCreateWebSocketThread(config).onSuccess(task -> {
                RocketChatWebSocketThread thread = task.getResult();
                thread.syncStateWith(config);
                return null;
            });
        }
    }

    private Task<RocketChatWebSocketThread> findOrCreateWebSocketThread(final ServerConfig config) {
        final String serverConfigId = config.getId();
        if (mWebSocketThreads.containsKey(serverConfigId)) {
            return Task.forResult(mWebSocketThreads.get(serverConfigId));
        } else {
            return RocketChatWebSocketThread.getStarted(getApplicationContext(), config)
                    .onSuccessTask(task -> {
                        mWebSocketThreads.put(serverConfigId, task.getResult());
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
