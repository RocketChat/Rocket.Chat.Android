package chat.rocket.android.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import bolts.Task;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.RxHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.ddp.base.ActiveUsersSubscriber;
import chat.rocket.android.service.ddp.base.LoginServiceConfigurationSubscriber;
import chat.rocket.android.service.ddp.base.UserDataSubscriber;
import chat.rocket.android.service.ddp.stream.StreamRoomMessage;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.android.service.observer.FileUploadingToUrlObserver;
import chat.rocket.android.service.observer.FileUploadingWithUfsObserver;
import chat.rocket.android.service.observer.GcmPushRegistrationObserver;
import chat.rocket.android.service.observer.GetUsersOfRoomsProcedureObserver;
import chat.rocket.android.service.observer.LoadMessageProcedureObserver;
import chat.rocket.android.service.observer.MethodCallObserver;
import chat.rocket.android.service.observer.NewMessageObserver;
import chat.rocket.android.service.observer.PushSettingsObserver;
import chat.rocket.android.service.observer.SessionObserver;
import chat.rocket.android.service.observer.TokenLoginObserver;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import hugo.weaving.DebugLog;
import rx.Single;
import rx.subscriptions.CompositeSubscription;

/**
 * Thread for handling WebSocket connection.
 */
public class RocketChatWebSocketThread extends HandlerThread {
  private static final Class[] REGISTERABLE_CLASSES = {
      LoginServiceConfigurationSubscriber.class,
      ActiveUsersSubscriber.class,
      UserDataSubscriber.class,
      TokenLoginObserver.class,
      MethodCallObserver.class,
      SessionObserver.class,
      LoadMessageProcedureObserver.class,
      GetUsersOfRoomsProcedureObserver.class,
      NewMessageObserver.class,
      CurrentUserObserver.class,
      FileUploadingToUrlObserver.class,
      FileUploadingWithUfsObserver.class,
      PushSettingsObserver.class,
      GcmPushRegistrationObserver.class
  };
  private final Context appContext;
  private final String hostname;
  private final RealmHelper realmHelper;
  private final ConnectivityManagerInternal connectivityManager;
  private final ArrayList<Registrable> listeners = new ArrayList<>();
  private DDPClientWrapper ddpClient;
  private boolean listenersRegistered;
  private RocketChatCache rocketChatCache;
  private final DDPClientRef ddpClientRef = new DDPClientRef() {
    @Override
    public DDPClientWrapper get() {
      return ddpClient;
    }
  };

  private static class KeepAliveTimer {
    private long lastTime;
    private final long thresholdMs;

    public KeepAliveTimer(long thresholdMs) {
      this.thresholdMs = thresholdMs;
      lastTime = System.currentTimeMillis();
    }

    public boolean shouldCheckPrecisely() {
      return lastTime + thresholdMs < System.currentTimeMillis();
    }

    public void update() {
      lastTime = System.currentTimeMillis();
    }
  }

  private final KeepAliveTimer keepAliveTimer = new KeepAliveTimer(20000);

  private RocketChatWebSocketThread(Context appContext, String hostname) {
    super("RC_thread_" + hostname);
    this.appContext = appContext;
    this.hostname = hostname;
    this.realmHelper = RealmStore.getOrCreate(hostname);
    this.connectivityManager = ConnectivityManager.getInstanceForInternal(appContext);
    this.rocketChatCache = new RocketChatCache(appContext);
  }

  /**
   * create new Thread.
   */
  @DebugLog
  public static Single<RocketChatWebSocketThread> getStarted(Context appContext, String hostname) {
    return Single.<RocketChatWebSocketThread>fromEmitter(objectSingleEmitter -> {
      new RocketChatWebSocketThread(appContext, hostname) {
        @Override
        protected void onLooperPrepared() {
          try {
            super.onLooperPrepared();
            objectSingleEmitter.onSuccess(this);
          } catch (Exception exception) {
            objectSingleEmitter.onError(exception);
          }
        }
      }.start();
    }).flatMap(webSocket ->
        webSocket.connect().map(_val -> webSocket));
  }

  @Override
  protected void onLooperPrepared() {
    super.onLooperPrepared();
    forceInvalidateTokens();
  }

  private void forceInvalidateTokens() {
    realmHelper.executeTransaction(realm -> {
      RealmSession session = RealmSession.queryDefaultSession(realm).findFirst();
      if (session != null
          && !TextUtils.isEmpty(session.getToken())
          && (session.isTokenVerified() || !TextUtils.isEmpty(session.getError()))) {
        session.setTokenVerified(false);
        session.setError(null);
      }
      return null;
    }).continueWith(new LogIfError());
  }

  /**
   * terminate WebSocket thread.
   */
  @DebugLog
  public Single<Boolean> terminate() {
    if (isAlive()) {
      return Single.fromEmitter(emitter -> {
        new Handler(getLooper()).post(() -> {
          RCLog.d("thread %s: terminated()", Thread.currentThread().getId());
          unregisterListenersAndClose();
          connectivityManager.notifyConnectionLost(hostname,
              ConnectivityManagerInternal.REASON_CLOSED_BY_USER);
          RocketChatWebSocketThread.super.quit();
          emitter.onSuccess(true);
        });
      });
    } else {
      connectivityManager.notifyConnectionLost(hostname,
          ConnectivityManagerInternal.REASON_NETWORK_ERROR);
      super.quit();
      return Single.just(true);
    }
  }

  /**
   * THIS METHOD THROWS EXCEPTION!! Use terminate() instead!!
   */
  @Deprecated
  @Override
  public final boolean quit() {
    throw new UnsupportedOperationException();
  }

  /**
   * synchronize the state of the thread with ServerConfig.
   */
  @DebugLog
  public Single<Boolean> keepAlive() {
    return checkIfConnectionAlive()
        .flatMap(alive -> alive ? Single.just(true) : connect());
  }

  private Single<Boolean> checkIfConnectionAlive() {
    if (ddpClient == null) {
      return Single.just(false);
    }

    if (!keepAliveTimer.shouldCheckPrecisely()) {
      return Single.just(true);
    }
    keepAliveTimer.update();

    return Single.fromEmitter(emitter -> {
      new Thread() {
        @Override
        public void run() {
          ddpClient.ping().continueWith(task -> {
            if (task.isFaulted()) {
              Exception error = task.getError();
              RCLog.e(error);
              emitter.onSuccess(false);
            } else {
              keepAliveTimer.update();
              emitter.onSuccess(true);
            }
            return null;
          });
        }
      }.start();
    });
  }

  private Single<Boolean> prepareDDPClient() {
    return checkIfConnectionAlive()
        .doOnSuccess(alive -> {
          if (!alive) {
            RCLog.d("DDPClient#create");
            ddpClient = DDPClientWrapper.create(hostname);
          }
        });
  }

  private Single<Boolean> connectDDPClient() {
    return prepareDDPClient()
        .flatMap(_val -> Single.fromEmitter(emitter -> {
          ServerInfo info = connectivityManager.getServerInfoForHost(hostname);
          RCLog.d("DDPClient#connect");
          ddpClient.connect(info.getSession(), info.isSecure())
              .onSuccessTask(task -> {
                final String newSession = task.getResult().session;
                connectivityManager.notifyConnectionEstablished(hostname, newSession);

                // handling WebSocket#onClose() callback.
                task.getResult().client.getOnCloseCallback().onSuccess(_task -> {
                  ddpClient.close();
                  forceInvalidateTokens();
                  connectivityManager.notifyConnecting(hostname);
                  // Needed to use subscriptions because of legacy code.
                  // TODO: Should update to RxJava 2
                  final CompositeSubscription subscriptions = new CompositeSubscription();
                  subscriptions.add(
                    connect().retryWhen(RxHelper.exponentialBackoff(3, 500, TimeUnit.MILLISECONDS))
                    .subscribe(
                        connected -> {
                          if (!connected) {
                            connectivityManager.notifyConnectionLost(
                                    hostname, ConnectivityManagerInternal.REASON_NETWORK_ERROR
                            );
                          }
                          subscriptions.clear();
                        },
                        err -> logErrorAndUnsubscribe(subscriptions, err)
                    )
                  );
                  return null;
                });

                return realmHelper.executeTransaction(realm -> {
                  RealmSession sessionObj = RealmSession.queryDefaultSession(realm).findFirst();
                  if (sessionObj == null) {
                    realm.createOrUpdateObjectFromJson(RealmSession.class,
                        new JSONObject().put(RealmSession.ID, RealmSession.DEFAULT_ID));
                  } else {
                    // invalidate login token.
                    if (!TextUtils.isEmpty(sessionObj.getToken()) && sessionObj.isTokenVerified()) {
                      sessionObj.setTokenVerified(false);
                      sessionObj.setError(null);
                    }

                  }
                  return null;
                });
              })
              .continueWith(task -> {
                if (task.isFaulted()) {
                  emitter.onError(task.getError());
                } else {
                  emitter.onSuccess(true);
                }
                return null;
              });
        }));
  }

  private void logErrorAndUnsubscribe(CompositeSubscription subscriptions, Throwable err) {
    RCLog.e(err);
    subscriptions.clear();
  }

  @DebugLog
  private Single<Boolean> connect() {
    return connectDDPClient()
        .flatMap(_val -> Single.fromEmitter(emitter -> {
          fetchPublicSettings();
          fetchPermissions();
          registerListeners();
          emitter.onSuccess(true);
        }));
  }

  private Task<Void> fetchPublicSettings() {
    return new MethodCallHelper(realmHelper, ddpClientRef).getPublicSettings();
  }

  private Task<Void> fetchPermissions() {
    return new MethodCallHelper(realmHelper, ddpClientRef).getPermissions();
  }

  @DebugLog
  private void registerListeners() {
    if (!Thread.currentThread().getName().equals("RC_thread_" + hostname)) {
      // execute in Looper.
      new Handler(getLooper()).post(this::registerListeners);
      return;
    }

    if (listenersRegistered) {
      unregisterListeners();
    }
    listenersRegistered = true;

    for (Class clazz : REGISTERABLE_CLASSES) {
      try {
        Constructor ctor = clazz.getConstructor(Context.class, String.class, RealmHelper.class,
            DDPClientRef.class);
        Object obj = ctor.newInstance(appContext, hostname, realmHelper, ddpClientRef);

        if (obj instanceof Registrable) {
          Registrable registrable = (Registrable) obj;
          registrable.register();
          listeners.add(registrable);
        }
        // Register for room stream messages
        String roomId = rocketChatCache.getSelectedRoomId();
        if (roomId != null && !roomId.isEmpty()) {
          StreamRoomMessage streamRoomMessage = new StreamRoomMessage(
                  appContext, hostname, realmHelper, ddpClientRef, roomId
          );
          streamRoomMessage.register();
          listeners.add(streamRoomMessage);
        }
      } catch (Exception exception) {
        RCLog.w(exception, "Failed to register listeners!!");
      }
    }
  }

  @DebugLog
  private void unregisterListenersAndClose() {
   unregisterListeners();
    if (ddpClient != null) {
      ddpClient.close();
      ddpClient = null;
    }
  }

  @DebugLog
  private void unregisterListeners() {
    Iterator<Registrable> iterator = listeners.iterator();
    while (iterator.hasNext()) {
      Registrable registrable = iterator.next();
      registrable.unregister();
      iterator.remove();
    }
    listenersRegistered = false;
  }
}
