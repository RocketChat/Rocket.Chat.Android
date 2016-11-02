package chat.rocket.android.service;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.service.ddp_subscription.LoginServiceConfigurationSubscriber;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import chat.rocket.android_ddp.DDPClient;
import hugo.weaving.DebugLog;
import jp.co.crowdworks.realm_java_helpers.RealmHelper;
import timber.log.Timber;

import static android.content.ContentValues.TAG;

public class RocketChatWebSocketThread extends HandlerThread {
    private final Context mAppContext;
    private final String mServerConfigId;
    private RocketChatWebSocketAPI mWebSocketAPI;
    private boolean mSocketExists;
    private boolean mListenersRegistered;

    private RocketChatWebSocketThread(Context appContext, String id) {
        super("RC_thread_"+id);
        mServerConfigId = id;
        mAppContext = appContext;
    }

    @DebugLog
    public static Task<RocketChatWebSocketThread> getStarted(Context appContext, ServerConfig config) {
        TaskCompletionSource<RocketChatWebSocketThread> task = new TaskCompletionSource<>();
        new RocketChatWebSocketThread(appContext, config.getId()){
            @Override
            protected void onLooperPrepared() {
                try {
                    super.onLooperPrepared();
                    task.setResult(this);
                }
                catch (Exception e) {
                    task.setError(e);
                }
            }
        }.start();
        return task.getTask();
    }

    @DebugLog
    public static void terminate(RocketChatWebSocketThread t) {
        t.quit();
    }

    private Task<Void> ensureConnection() {
        if (mWebSocketAPI == null || !mWebSocketAPI.isConnected()) {
            return registerListeners();
        }
        else return Task.forResult(null);
    }

    @DebugLog
    public void syncStateWith(ServerConfig config) {
        if (config == null || TextUtils.isEmpty(config.getHostname()) || !TextUtils.isEmpty(config.getConnectionError())) {
            quit();
        }
        else {
            ensureConnection()
                    .continueWith(task -> {
                        new Handler(getLooper()).post(() -> {
                            keepaliveListeners();
                        });
                        return null;
                    });
        }
    }

    @Override
    protected void onLooperPrepared() {
        super.onLooperPrepared();

        registerListeners();
    }

    @Override
    public boolean quit() {
        scheduleUnregisterListeners();
        return super.quit();
    }

    @Override
    public boolean quitSafely() {
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

    private static final Class[] REGISTERABLE_CLASSES = {
            LoginServiceConfigurationSubscriber.class
    };

    private final ArrayList<Registerable> mListeners = new ArrayList<>();

    private void prepareWebSocket() {
        ServerConfig config = RealmHelper.executeTransactionForRead(realm -> realm.where(ServerConfig.class).equalTo("id", mServerConfigId).findFirst());
        if (mWebSocketAPI == null || !mWebSocketAPI.isConnected()) {
            mWebSocketAPI = RocketChatWebSocketAPI.create(config.getHostname());
        }
    }

    @DebugLog
    private Task<Void> registerListeners(){
        if (mSocketExists) return Task.forResult(null);

        mSocketExists = true;
        prepareWebSocket();
        return mWebSocketAPI.connect().onSuccess(task -> {
            registerListenersActually();

            DDPClient client = task.getResult().client;

            // handling WebSocket#onClose() callback.
            client.getOnCloseCallback().onSuccess(_task -> {
                quit();
                return null;
            });

            // just for debugging.
            client.getSubscriptionCallback().subscribe(event -> {
                Timber.d(TAG, "Callback [DEBUG] < " + event);
            });

            return null;
        }).continueWith(task -> {
            if (task.isFaulted()) {
                ServerConfig.logError(mServerConfigId, task.getError());
            }
            return null;
        });
    }

    //@DebugLog
    private void registerListenersActually() {
        if (mListenersRegistered) return;
        mListenersRegistered = true;

        for(Class clazz: REGISTERABLE_CLASSES){
            try {
                Constructor ctor = clazz.getConstructor(Context.class, RocketChatWebSocketAPI.class);
                Object obj = ctor.newInstance(mAppContext, mWebSocketAPI);

                if(obj instanceof Registerable) {
                    Registerable l = (Registerable) obj;
                    l.register();
                    mListeners.add(l);
                }
            } catch (Exception e) {
                Timber.w(e);
            }
        }
    }

    //@DebugLog
    private void keepaliveListeners(){
        if (!mSocketExists || !mListenersRegistered) return;

        for (Registerable l : mListeners) l.keepalive();
    }

    //@DebugLog
    private void unregisterListeners(){
        if (!mSocketExists || !mListenersRegistered) return;

        Iterator<Registerable> it = mListeners.iterator();
        while(it.hasNext()){
            Registerable l = it.next();
            l.unregister();
            it.remove();
        }
        if (mWebSocketAPI != null) {
            mWebSocketAPI.close();
            mWebSocketAPI = null;
        }
        mListenersRegistered = false;
        mSocketExists = false;
    }

}
