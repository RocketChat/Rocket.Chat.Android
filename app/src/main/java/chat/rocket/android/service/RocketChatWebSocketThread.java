package chat.rocket.android.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;
import bolts.Task;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;
import chat.rocket.android.service.ddp.base.ActiveUsersSubscriber;
import chat.rocket.android.service.ddp.base.LoginServiceConfigurationSubscriber;
import chat.rocket.android.service.ddp.base.UserDataSubscriber;
import chat.rocket.android.service.observer.CurrentUserObserver;
import chat.rocket.android.service.observer.FileUploadingToS3Observer;
import chat.rocket.android.service.observer.FileUploadingWithUfsObserver;
import chat.rocket.android.service.observer.GcmPushRegistrationObserver;
import chat.rocket.android.service.observer.GetUsersOfRoomsProcedureObserver;
import chat.rocket.android.service.observer.LoadMessageProcedureObserver;
import chat.rocket.android.service.observer.MethodCallObserver;
import chat.rocket.android.service.observer.NewMessageObserver;
import chat.rocket.android.service.observer.PushSettingsObserver;
import chat.rocket.android.service.observer.SessionObserver;
import chat.rocket.android.service.observer.TokenLoginObserver;
import hugo.weaving.DebugLog;
import rx.Completable;
import rx.Single;

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
      FileUploadingToS3Observer.class,
      FileUploadingWithUfsObserver.class,
      PushSettingsObserver.class,
      GcmPushRegistrationObserver.class
  };
  private final Context appContext;
  private final String hostname;
  private final RealmHelper serverConfigRealm;
  private final ConnectivityManagerInternal connectivityManager;
  private final ArrayList<Registrable> listeners = new ArrayList<>();
  private DDPClientWrapper ddpClient;
  private boolean listenersRegistered;

  private RocketChatWebSocketThread(Context appContext, String hostname) {
    super("RC_thread_" + hostname);
    this.appContext = appContext;
    this.hostname = hostname;
    this.serverConfigRealm = RealmStore.getOrCreate(hostname);
    this.connectivityManager = ConnectivityManager.getInstanceForInternal(appContext);
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
        webSocket.connect().andThen(Single.just(webSocket)));
  }

  @Override
  protected void onLooperPrepared() {
    super.onLooperPrepared();
    forceInvalidateTokens();
  }

  private void forceInvalidateTokens() {
    serverConfigRealm.executeTransaction(realm -> {
      Session session = Session.queryDefaultSession(realm).findFirst();
      if (session != null
          && !TextUtils.isEmpty(session.getToken())
          && (session.isTokenVerified() || !TextUtils.isEmpty(session.getError()))) {
        session.setTokenVerified(false);
        session.setError(null);
      }
      return null;
    }).continueWith(new LogcatIfError());
  }

  /**
   * terminate WebSocket thread.
   */
  @DebugLog
  public Completable terminate() {
    if (isAlive()) {
      return Completable.fromEmitter(completableEmitter -> {
        new Handler(getLooper()).post(() -> {
          RCLog.d("thread %s: terminated()", Thread.currentThread().getId());
          unregisterListeners();
          connectivityManager.notifyConnectionLost(hostname,
              ConnectivityManagerInternal.REASON_CLOSED_BY_USER);
          RocketChatWebSocketThread.super.quit();
          completableEmitter.onCompleted();
        });
      });
    } else {
      connectivityManager.notifyConnectionLost(hostname,
          ConnectivityManagerInternal.REASON_NETWORK_ERROR);
      super.quit();
      return Completable.complete();
    }
  }

  /**
   * THIS METHOD THROWS EXCEPTION!!
   * Use terminate() instead!!
   */
  @Deprecated public final boolean quit() {
    throw new UnsupportedOperationException();
  }

  /**
   * synchronize the state of the thread with ServerConfig.
   */
  @DebugLog
  public Completable keepAlive() {
    return checkIfConnectionAlive()
        .flatMapCompletable(alive -> alive ? Completable.complete() : connect());
  }

  private Single<Boolean> checkIfConnectionAlive() {
    if (ddpClient == null || !ddpClient.isConnected()) {
      return Single.just(false);
    }

    return Single.fromEmitter(booleanSingleEmitter -> {
      ddpClient.ping().continueWith(task -> {
        booleanSingleEmitter.onSuccess(!task.isFaulted());
        return null;
      });
    });
  }

  private Completable prepareDDPClient() {
    return checkIfConnectionAlive()
        .doOnSuccess(alive -> {
          if (!alive) {
            ddpClient = DDPClientWrapper.create(hostname);
          }
        })
        .toCompletable();
  }

  private Completable connectDDPClient() {
    return prepareDDPClient()
        .andThen(Completable.fromEmitter(completableEmitter -> {
          ServerInfo info = connectivityManager.getServerInfoForHost(hostname);
          ddpClient.connect(info.session, !info.insecure)
              .onSuccessTask(task -> {
                final String newSession = task.getResult().session;
                connectivityManager.notifyConnectionEstablished(hostname, newSession);

                // handling WebSocket#onClose() callback.
                task.getResult().client.getOnCloseCallback().onSuccess(_task -> {
                  if (listenersRegistered) {
                    terminate();
                  }
                  return null;
                });

                return serverConfigRealm.executeTransaction(realm -> {
                  Session sessionObj = Session.queryDefaultSession(realm).findFirst();
                  if (sessionObj == null) {
                    realm.createOrUpdateObjectFromJson(Session.class,
                        new JSONObject().put(Session.ID, Session.DEFAULT_ID));
                  }
                  return null;
                });
              })
              .continueWith(task -> {
                if (task.isFaulted()) {
                  completableEmitter.onError(task.getError());
                } else {
                  completableEmitter.onCompleted();
                }
                return null;
              });
        }));
  }

  @DebugLog
  private Completable connect() {
    return connectDDPClient()
        .andThen(Completable.fromEmitter(completableEmitter -> {
          fetchPublicSettings();
          registerListeners();
          completableEmitter.onCompleted();
        }));
  }

  private Task<Void> fetchPublicSettings() {
    return new MethodCallHelper(serverConfigRealm, ddpClient).getPublicSettings();
  }

  //@DebugLog
  private void registerListeners() {
    if (!Thread.currentThread().getName().equals("RC_thread_" + hostname)) {
      // execute in Looper.
      new Handler(getLooper()).post(this::registerListeners);
      return;
    }

    if (listenersRegistered) {
      return;
    }
    listenersRegistered = true;

    for (Class clazz : REGISTERABLE_CLASSES) {
      try {
        Constructor ctor = clazz.getConstructor(Context.class, String.class, RealmHelper.class,
            DDPClientWrapper.class);
        Object obj = ctor.newInstance(appContext, hostname, serverConfigRealm, ddpClient);

        if (obj instanceof Registrable) {
          Registrable registrable = (Registrable) obj;
          registrable.register();
          listeners.add(registrable);
        }
      } catch (Exception exception) {
        RCLog.w(exception, "Failed to register listeners!!");
      }
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
    if (ddpClient != null) {
      ddpClient.close();
      ddpClient = null;
    }
  }
}
