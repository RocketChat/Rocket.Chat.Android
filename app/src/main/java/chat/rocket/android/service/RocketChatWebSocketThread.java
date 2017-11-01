package chat.rocket.android.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import bolts.Task;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.helper.RxHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.service.ddp.base.ActiveUsersSubscriber;
import chat.rocket.android.service.ddp.base.LoginServiceConfigurationSubscriber;
import chat.rocket.android.service.ddp.base.UserDataSubscriber;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.android.service.observer.DeletedMessageObserver;
import chat.rocket.android.service.observer.FileUploadingToUrlObserver;
import chat.rocket.android.service.observer.FileUploadingWithUfsObserver;
import chat.rocket.android.service.observer.GcmPushRegistrationObserver;
import chat.rocket.android.service.observer.GetUsersOfRoomsProcedureObserver;
import chat.rocket.android.service.observer.LoadMessageProcedureObserver;
import chat.rocket.android.service.observer.MethodCallObserver;
import chat.rocket.android.service.observer.NewMessageObserver;
import chat.rocket.android.service.observer.PushSettingsObserver;
import chat.rocket.android.service.observer.SessionObserver;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import hugo.weaving.DebugLog;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import rx.Completable;
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
      MethodCallObserver.class,
      SessionObserver.class,
      LoadMessageProcedureObserver.class,
      GetUsersOfRoomsProcedureObserver.class,
      NewMessageObserver.class,
      DeletedMessageObserver.class,
      CurrentUserObserver.class,
      FileUploadingToUrlObserver.class,
      FileUploadingWithUfsObserver.class,
      PushSettingsObserver.class,
      GcmPushRegistrationObserver.class
  };
  private static final long HEARTBEAT_PERIOD_MS = 20000;
  private final Context appContext;
  private final String hostname;
  private final RealmHelper realmHelper;
  private final ConnectivityManagerInternal connectivityManager;
  private final ArrayList<Registrable> listeners = new ArrayList<>();
  private final CompositeDisposable hearbeatDisposable = new CompositeDisposable();
  private final CompositeSubscription reconnectSubscription = new CompositeSubscription();
  private DDPClientWrapper ddpClient;
  private boolean listenersRegistered;
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
  }

  /**
   * build new Thread.
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
        .flatMap(alive -> alive ? Single.just(true) : connectWithExponentialBackoff());
  }

  @DebugLog
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
              connectivityManager.notifyConnectionLost(
                      hostname, ConnectivityManagerInternal.REASON_NETWORK_ERROR);
              emitter.onError(error);
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

  @DebugLog
  private Flowable<Boolean> heartbeat(long interval) {
    return Flowable.interval(interval, TimeUnit.MILLISECONDS)
            .onBackpressureDrop()
            .flatMap(tick -> ddpClient.doPing().toFlowable())
            .map(callback -> {
              if (callback instanceof DDPClientCallback.Ping) {
                return true;
              }
              // ideally we should never get here. We should always receive a DDPClientCallback.Ping
              // because we just received a pong. But maybe we received a pong from an unmatched
              // ping id which we should ignore. In this case or any other random error, log and
              // send false downstream
              RCLog.d("heartbeat pong < %s", callback.toString());
              return false;
            });
  }

  private Single<Boolean> prepareDDPClient() {
    // TODO: temporarily replaced checkIfConnectionAlive() call for this single checking if ddpClient is
    // null or not. In case it is, build a new client, otherwise just keep connecting with existing one.
    return Single.just(ddpClient != null)
        .doOnSuccess(alive -> {
          if (!alive) {
            RCLog.d("DDPClient#build");
            ddpClient = DDPClientWrapper.create(hostname);
          }
        });
  }

  private Single<Boolean> connectDDPClient() {
    return prepareDDPClient()
        .flatMap(_val -> Single.fromEmitter(emitter -> {
          ServerInfo info = connectivityManager.getServerInfoForHost(hostname);
          if (info == null) {
            emitter.onSuccess(false);
            return;
          }
          RCLog.d("DDPClient#connect");
          ddpClient.connect(info.getSession(), info.isSecure())
              .onSuccessTask(task -> {
                final String newSession = task.getResult().session;
                connectivityManager.notifyConnectionEstablished(hostname, newSession);

                // handling WebSocket#onClose() callback.
                task.getResult().client.getOnCloseCallback().onSuccess(_task -> {
                  reconnect();
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

  private void reconnect() {
    // if we are already trying to reconnect then return.
    if (reconnectSubscription.hasSubscriptions()) {
      return;
    }
    ddpClient.close();
    forceInvalidateTokens();
    connectivityManager.notifyConnecting(hostname);
    // Needed to use subscriptions because of legacy code.
    // TODO: Should update to RxJava 2
    reconnectSubscription.add(
            connectWithExponentialBackoff()
                    .subscribe(
                            connected -> {
                              if (!connected) {
                                connectivityManager.notifyConnecting(hostname);
                              }
                              reconnectSubscription.clear();
                            },
                            err -> logErrorAndUnsubscribe(reconnectSubscription, err)
                    )
    );
  }

  private void logErrorAndUnsubscribe(CompositeSubscription subscriptions, Throwable err) {
    RCLog.e(err);
    subscriptions.clear();
  }

  private Single<Boolean> connectWithExponentialBackoff() {
    return connect().retryWhen(RxHelper.exponentialBackoff(Integer.MAX_VALUE, 500, TimeUnit.MILLISECONDS));
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
    return new MethodCallHelper(appContext, realmHelper, ddpClientRef).getPublicSettings(hostname);
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

    List<RealmSession> sessions = realmHelper.executeTransactionForReadResults(realm ->
            realm.where(RealmSession.class)
                    .isNotNull(RealmSession.TOKEN)
                    .equalTo(RealmSession.TOKEN_VERIFIED, false)
                    .isNull(RealmSession.ERROR)
                    .findAll());

    if (sessions != null && sessions.size() > 0) {
      // if we have a session try to resume it. At this point we're probably recovering from
      // a disconnection state
      final CompositeSubscription subscriptions = new CompositeSubscription();
      MethodCallHelper methodCall = new MethodCallHelper(realmHelper, ddpClientRef);
      subscriptions.add(
              Completable.defer(() -> {
                Task<Void> result = methodCall.loginWithToken(sessions.get(0).getToken());
                if (result.isFaulted()) {
                  return Completable.error(result.getError());
                } else {
                  return Completable.complete();
                }
              }).retryWhen(RxHelper.exponentialBackoff(Integer.MAX_VALUE, 500, TimeUnit.MILLISECONDS))
                .subscribe(
                      () -> {
                        createObserversAndRegister();
                        subscriptions.clear();
                      },
                      error -> logErrorAndUnsubscribe(subscriptions, error)
              )
      );
    } else {
      // if we don't have any session then just build the observers and register normally
      createObserversAndRegister();
    }
  }

  @DebugLog
  private void createObserversAndRegister() {
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
      } catch (Exception exception) {
        RCLog.w(exception, "Failed to register listeners!!");
      }
    }
    listenersRegistered = true;
    startHeartBeat();
  }

  private void startHeartBeat() {
    hearbeatDisposable.clear();
    hearbeatDisposable.add(
        heartbeat(HEARTBEAT_PERIOD_MS)
            .subscribe(
                    ponged -> {
                      if (!ponged) {
                        RCLog.d("Pong received but didn't match ping id");
                      }
                    },
                    error -> {
                      RCLog.e(error);
                      // Stop pinging
                      hearbeatDisposable.clear();
                      if (error instanceof DDPClientCallback.Closed) {
                        RCLog.d("Hearbeat failure: retrying connection...");
                        reconnect();
                      }
                    }
            )
    );
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
    hearbeatDisposable.clear();
    listenersRegistered = false;
  }
}
