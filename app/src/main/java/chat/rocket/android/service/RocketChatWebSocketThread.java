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
import java.util.concurrent.TimeoutException;

import bolts.Task;
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
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.rx.RxWebSocketCallback;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.internal.RealmSession;
import hugo.weaving.DebugLog;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;

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
    private final CompositeDisposable heartbeatDisposable = new CompositeDisposable();
    private final CompositeDisposable reconnectDisposable = new CompositeDisposable();
    private boolean listenersRegistered;

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
    /* package */ static Single<RocketChatWebSocketThread> getStarted(Context appContext, String hostname) {
        return Single.<RocketChatWebSocketThread>create(objectSingleEmitter -> {
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
                webSocket.connectWithExponentialBackoff().map(_val -> webSocket));
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
    /* package */ Single<Boolean> terminate() {
        if (isAlive()) {
            return Single.create(emitter -> {
                new Handler(getLooper()).post(() -> {
                    RCLog.d("thread %s: terminated()", Thread.currentThread().getId());
                    unregisterListenersAndClose();
                    connectivityManager.notifyConnectionLost(hostname,
                            DDPClient.REASON_CLOSED_BY_USER);
                    RocketChatWebSocketThread.super.quit();
                    emitter.onSuccess(true);
                });
            });
        } else {
            connectivityManager.notifyConnectionLost(hostname,
                    DDPClient.REASON_NETWORK_ERROR);
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
    /* package */ Single<Boolean> keepAlive() {
        return checkIfConnectionAlive()
                .flatMap(alive -> alive ? Single.just(true) : connectWithExponentialBackoff());
    }

    @DebugLog
    private Single<Boolean> checkIfConnectionAlive() {
        if (DDPClient.get() == null) {
            return Single.just(false);
        }

        return Single.create(emitter -> {
            new Thread() {
                @Override
                public void run() {
                    DDPClient.get().ping().continueWith(task -> {
                        if (task.isFaulted()) {
                            Exception error = task.getError();
                            RCLog.e(error);
                            connectivityManager.notifyConnectionLost(
                                    hostname, DDPClient.REASON_CLOSED_BY_USER);
                            emitter.onSuccess(false);
                        } else {
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
                .flatMap(tick -> DDPClient.get().doPing().toFlowable())
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

    private Single<Boolean> connectDDPClient() {
        return Single.create(emitter -> {
            ServerInfo info = connectivityManager.getServerInfoForHost(hostname);
            if (info == null) {
                emitter.onSuccess(false);
                return;
            }
            RCLog.d("DDPClient#connect");
            connectivityManager.notifyConnecting(hostname);
            DDPClient.get().connect(hostname, info.getSession(), info.isSecure())
                    .onSuccessTask(task -> {
                        final String newSession = task.getResult().session;
                        connectivityManager.notifyConnectionEstablished(hostname, newSession);
                        // handling WebSocket#onClose() callback.
                        task.getResult().client.getOnCloseCallback().onSuccess(_task -> {
                            RxWebSocketCallback.Close result = _task.getResult();
                            if (result.code == DDPClient.REASON_NETWORK_ERROR) {
                                reconnect();
                            }
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
        });
    }

    private void reconnect() {
        // if we are already trying to reconnect then return.
        if (reconnectDisposable.size() > 0) {
            return;
        }
        forceInvalidateTokens();
        reconnectDisposable.add(
                connectWithExponentialBackoff()
                        .subscribe(connected -> {
                                    if (!connected) {
                                        connectivityManager.notifyConnectionLost(hostname,
                                                DDPClient.REASON_NETWORK_ERROR);
                                    }
                                    reconnectDisposable.clear();
                                }, error -> {
                                    connectivityManager.notifyConnectionLost(hostname,
                                            DDPClient.REASON_NETWORK_ERROR);
                                    logErrorAndUnsubscribe(reconnectDisposable, error);
                                }
                        )
        );
    }

    private void logErrorAndUnsubscribe(CompositeDisposable disposables, Throwable err) {
        RCLog.e(err);
        disposables.clear();
    }

    private Single<Boolean> connectWithExponentialBackoff() {
        return connect()
                .retryWhen(RxHelper.exponentialBackoff(1, 250, TimeUnit.MILLISECONDS))
                .onErrorResumeNext(Single.just(false));
    }

    @DebugLog
    private Single<Boolean> connect() {
        return connectDDPClient()
                .flatMap(_val -> Single.create(emitter -> {
                    fetchPublicSettings();
                    fetchPermissions();
                    registerListeners();
                    emitter.onSuccess(true);
                }));
    }

    private Task<Void> fetchPublicSettings() {
        return new MethodCallHelper(appContext, realmHelper).getPublicSettings(hostname);
    }

    private Task<Void> fetchPermissions() {
        return new MethodCallHelper(realmHelper).getPermissions();
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
            final CompositeDisposable disposables = new CompositeDisposable();
            MethodCallHelper methodCall = new MethodCallHelper(realmHelper);
            disposables.add(
                    Completable.defer(() -> {
                        Task<Void> result = methodCall.loginWithToken(sessions.get(0).getToken());
                        if (result.isFaulted()) {
                            return Completable.error(result.getError());
                        } else {
                            return Completable.complete();
                        }
                    }).retryWhen(RxHelper.exponentialBackoff(3, 500, TimeUnit.MILLISECONDS))
                            .subscribe(
                                    () -> {
                                        createObserversAndRegister();
                                        disposables.clear();
                                    },
                                    error -> logErrorAndUnsubscribe(disposables, error)
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
                Constructor ctor = clazz.getConstructor(Context.class, String.class, RealmHelper.class);
                Object obj = ctor.newInstance(appContext, hostname, realmHelper);

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
        heartbeatDisposable.clear();
        heartbeatDisposable.add(
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
                                    heartbeatDisposable.clear();
                                    if (error instanceof DDPClientCallback.Closed || error instanceof TimeoutException) {
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
        DDPClient.get().close();
    }

    @DebugLog
    private void unregisterListeners() {
        Iterator<Registrable> iterator = listeners.iterator();
        while (iterator.hasNext()) {
            Registrable registrable = iterator.next();
            registrable.unregister();
            iterator.remove();
        }
        heartbeatDisposable.clear();
        listenersRegistered = false;
    }
}
