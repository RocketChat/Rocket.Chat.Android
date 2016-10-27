package chat.rocket.android.api.ws;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.Constants;
import chat.rocket.android.content.Registerable;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.observer.AddRoomHandler;
import chat.rocket.android.content.observer.LoginHandler;
import chat.rocket.android.content.observer.MarkRoomAsReadHandler;
import chat.rocket.android.content.observer.MethodCall2Observer;
import chat.rocket.android.content.observer.RocketChatRoom;
import chat.rocket.android.content.observer.SendNewMessageHandler;
import chat.rocket.android.content.observer.UserStatusObserver;
import chat.rocket.android.content.subscriber.FilteredUsers;
import chat.rocket.android.content.subscriber.LoginServiceConfiguration;
import chat.rocket.android.content.subscriber.UserData;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.DDPSubscription;
import hugo.weaving.DebugLog;
import rx.functions.Action1;

public class RocketChatWSService extends Service {
    private final static String TAG = Constants.LOG_TAG;

    private static final Class[] HANDLER_CLASSES = {
            LoginHandler.class
            , RocketChatRoom.class
            , AddRoomHandler.class
            , SendNewMessageHandler.class
            , UserData.class
            , UserStatusObserver.class
            , MarkRoomAsReadHandler.class
            , LoginServiceConfiguration.class
            , FilteredUsers.class

            , MethodCall2Observer.class
    };

    private RocketChatWSAPI mAPI;

    private Looper mRegisterThreadLooper;

    /**
     * Ensure RocketChatWSService alive.
     *
     * @param context
     * @return true if Service is started just now. false if Service is already started.
     */
    public static boolean keepalive(Context context) {
        return context.startService(new Intent(context, RocketChatWSService.class)) == null;
    }

    public static void kill(Context context) {
        context.stopService(new Intent(context, RocketChatWSService.class));
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();

        final ServerConfig s = RocketChatDatabaseHelper.read(getBaseContext(), new RocketChatDatabaseHelper.DBCallback<ServerConfig>() {
            @Override
            public ServerConfig process(SQLiteDatabase db) throws Exception {
                return ServerConfig.getPrimaryConfig(db);
            }
        });

        if (s==null) {
            stopSelf();
            return;
        }

        s.syncstate = SyncState.NOT_SYNCED;
        s.putByContentProvider(getBaseContext());

        mAPI = new RocketChatWSAPI(s.hostname);
        mAPI.connect().onSuccess(new Continuation<DDPClientCallback.Connect, Object>() {
            @Override
            public Object then(Task<DDPClientCallback.Connect> task) throws Exception {
                DDPClientCallback.Connect result = task.getResult();

                // disconnect if 'Failure' event received.
                result.client.getFailureObservable().subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        stopSelf();
                    }
                });

                // just for debugging.
                result.client.getSubscriptionCallback().subscribe(new Action1<DDPSubscription.Event>() {
                    @Override
                    public void call(DDPSubscription.Event event) {
                        Log.d(TAG,"Callback [DEBUG] < "+ event);
                    }
                });

                HandlerThread regThread = new HandlerThread("register");
                regThread.start();
                mRegisterThreadLooper = regThread.getLooper();

                registerListeners();

                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                if(task.isFaulted()){
                    Log.e(TAG, "websocket: failed to connect.", task.getError());
                    if(s.authType == ServerConfig.AuthType.UNSPECIFIED) {
                        s.deleteByContentProvider(getBaseContext());
                    }
                    else {
                        s.syncstate = SyncState.FAILED;
                        s.putByContentProvider(getBaseContext());
                    }
                    stopSelf();
                }

                return null;
            }
        });
    }

    private final ArrayList<Registerable> mListeners = new ArrayList<>();

    private void registerListeners(){
        final Context context = getApplicationContext();
        for(Class clazz: HANDLER_CLASSES){
            try {
                Constructor ctor = clazz.getConstructor(Context.class, Looper.class, RocketChatWSAPI.class);
                Object obj = ctor.newInstance(context, mRegisterThreadLooper, mAPI);

                if(obj instanceof Registerable) {
                    Registerable l = (Registerable) obj;
                    l.register();
                    mListeners.add(l);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error", e);
            }
        }
    }

    private void unregisterListeners(){
        Iterator<Registerable> it = mListeners.iterator();
        while(it.hasNext()){
            Registerable l = it.next();
            l.unregister();
            it.remove();
        }
    }

    @Override
    public void onDestroy() {
        unregisterListeners();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
