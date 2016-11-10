package chat.rocket.android.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import bolts.Continuation;
import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.service.ddp_subscriber.LoginServiceConfigurationSubscriber;
import chat.rocket.android.service.observer.MethodCallObserver;
import chat.rocket.android.service.observer.SessionObserver;
import chat.rocket.android.service.observer.TokenLoginObserver;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import chat.rocket.android_ddp.DDPClientCallback;
import hugo.weaving.DebugLog;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import jp.co.crowdworks.realm_java_helpers.RealmHelper;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;
import timber.log.Timber;

/**
 * Thread for handling WebSocket connection.
 */
public class RocketChatWebSocketThread extends HandlerThread {
  private static final Class[] REGISTERABLE_CLASSES = {
      LoginServiceConfigurationSubscriber.class,
      TokenLoginObserver.class,
      MethodCallObserver.class,
      SessionObserver.class
  };
  private final Context appContext;
  private final String serverConfigId;
  private final ArrayList<Registerable> listeners = new ArrayList<>();
  private RocketChatWebSocketAPI webSocketAPI;
  private boolean socketExists;
  private boolean listenersRegistered;

  private RocketChatWebSocketThread(Context appContext, String serverConfigId) {
    super("RC_thread_" + serverConfigId);
    this.serverConfigId = serverConfigId;
    this.appContext = appContext;
  }

  /**
   * create new Thread.
   */
  @DebugLog public static Task<RocketChatWebSocketThread> getStarted(Context appContext,
      ServerConfig config) {
    TaskCompletionSource<RocketChatWebSocketThread> task = new TaskCompletionSource<>();
    new RocketChatWebSocketThread(appContext, config.getId()) {
      @Override protected void onLooperPrepared() {
        try {
          super.onLooperPrepared();
          task.setResult(this);
        } catch (Exception exception) {
          task.setError(exception);
        }
      }
    }.start();
    return task.getTask();
  }

  /**
   * terminate the thread.
   */
  @DebugLog public static void terminate(RocketChatWebSocketThread thread) {
    thread.quit();
  }

  private Task<Void> ensureConnection() {
    if (webSocketAPI == null || !webSocketAPI.isConnected()) {
      return registerListeners();
    } else {
      return Task.forResult(null);
    }
  }

  /**
   * synchronize the state of the thread with ServerConfig.
   */
  @DebugLog public void syncStateWith(ServerConfig config) {
    if (config == null || TextUtils.isEmpty(config.getHostname()) || !TextUtils.isEmpty(
        config.getConnectionError())) {
      quit();
    } else {
      ensureConnection().continueWith(task -> {
        new Handler(getLooper()).post(this::keepaliveListeners);
        return null;
      });
    }
  }

  @Override protected void onLooperPrepared() {
    super.onLooperPrepared();

    registerListeners();
  }

  @Override public boolean quit() {
    scheduleUnregisterListeners();
    return super.quit();
  }

  @Override public boolean quitSafely() {
    scheduleUnregisterListeners();
    return super.quitSafely();
  }

  private void scheduleUnregisterListeners() {
    if (isAlive()) {
      new Handler(getLooper()).post(() -> {
        Timber.d("thread %s: quit()", Thread.currentThread().getId());
        unregisterListeners();
      });
    }
  }

  private void prepareWebSocket(ServerConfig config) {
    if (webSocketAPI == null || !webSocketAPI.isConnected()) {
      webSocketAPI = RocketChatWebSocketAPI.create(config.getHostname());
    }
  }

  @DebugLog private Task<Void> registerListeners() {
    if (socketExists) {
      return Task.forResult(null);
    }
    socketExists = true;

    final ServerConfig config = RealmHelper.executeTransactionForRead(realm ->
        realm.where(ServerConfig.class).equalTo("id", serverConfigId).findFirst());

    prepareWebSocket(config);
    return webSocketAPI.connect(config.getSession()).onSuccessTask(task -> {
      final String session = task.getResult().session;
      RealmHelperBolts.executeTransaction(realm ->
          realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
              .put("id", serverConfigId)
              .put("session", session))
      ).continueWith(new LogcatIfError());
      return task;
    }).onSuccess(new Continuation<DDPClientCallback.Connect, Object>() {
      // TODO type detection doesn't work due to retrolambda's bug...
      @Override public Object then(Task<DDPClientCallback.Connect> task)
          throws Exception {
        registerListenersActually();

        // handling WebSocket#onClose() callback.
        task.getResult().client.getOnCloseCallback().onSuccess(_task -> {
          quit();
          return null;
        }).continueWith(_task -> {
          if (_task.isFaulted()) {
            ServerConfig.logConnectionError(serverConfigId, _task.getError());
          }
          return null;
        });

        return null;
      }
    }).continueWith(task -> {
      if (task.isFaulted()) {
        ServerConfig.logConnectionError(serverConfigId, task.getError());
      }
      return null;
    });
  }

  //@DebugLog
  private void registerListenersActually() {
    if (!Thread.currentThread().getName().equals("RC_thread_" + serverConfigId)) {
      // execute in Looper.
      new Handler(getLooper()).post(() -> {
        registerListenersActually();
      });
      return;
    }

    if (listenersRegistered) {
      return;
    }
    listenersRegistered = true;

    for (Class clazz : REGISTERABLE_CLASSES) {
      try {
        Constructor ctor = clazz.getConstructor(Context.class, String.class,
            RocketChatWebSocketAPI.class);
        Object obj = ctor.newInstance(appContext, serverConfigId, webSocketAPI);

        if (obj instanceof Registerable) {
          Registerable registerable = (Registerable) obj;
          registerable.register();
          listeners.add(registerable);
        }
      } catch (Exception exception) {
        Timber.w(exception, "Failed to register listeners!!");
      }
    }
  }

  //@DebugLog
  private void keepaliveListeners() {
    if (!socketExists || !listenersRegistered) {
      return;
    }

    for (Registerable registerable : listeners) {
      registerable.keepalive();
    }
  }

  //@DebugLog
  private void unregisterListeners() {
    if (!socketExists || !listenersRegistered) {
      return;
    }

    Iterator<Registerable> iterator = listeners.iterator();
    while (iterator.hasNext()) {
      Registerable registerable = iterator.next();
      registerable.unregister();
      iterator.remove();
    }
    if (webSocketAPI != null) {
      webSocketAPI.close();
      webSocketAPI = null;
    }
    listenersRegistered = false;
    socketExists = false;
  }
}
