package chat.rocket.android.api.ws;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Iterator;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.Constants;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.observer.AbstractObserver;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;
import jp.co.crowdworks.android_meteor.ddp.DDPClientCallback;
import jp.co.crowdworks.android_meteor.ddp.DDPSubscription;
import rx.Observable;

public class RocketChatWSService extends Service {
    private final static String TAG = Constants.LOG_TAG;

    RocketChatWSAPI mAPI;
    Observable<DDPSubscription.Event> mDDPCallback;

    public static void keepalive(Context context) {
        context.startService(new Intent(context, RocketChatWSService.class));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final ServerConfig s = RocketChatDatabaseHelper.read(getBaseContext(), new RocketChatDatabaseHelper.DBCallback<ServerConfig>() {
            @Override
            public ServerConfig process(SQLiteDatabase db) throws Exception {
                return ServerConfig.get(db, "is_primary = 1",null);
            }
        });

        if (s==null) {
            stopSelf();
            return;
        }

        mAPI = new RocketChatWSAPI(s.hostname);
        mAPI.connect().onSuccessTask(new Continuation<DDPClientCallback.Connect, Task<DDPClientCallback.RPC>>() {
            @Override
            public Task<DDPClientCallback.RPC> then(Task<DDPClientCallback.Connect> task) throws Exception {
                if(TextUtils.isEmpty(s.authToken)) {
                    return mAPI.login(s.account, s.passwd);
                }
                else {
                    DDPClientCallback.Connect result = task.getResult();
                    mDDPCallback = result.client.getSubscriptionCallback();

                    return mAPI.login(s.authToken);
                }
            }
        }).continueWith(new Continuation<DDPClientCallback.RPC, Object>() {
            @Override
            public Object then(Task<DDPClientCallback.RPC> task) throws Exception {
                if(task.isFaulted()){
                    s.syncstate = SyncState.SYNCED;
                    RocketChatDatabaseHelper.write(getBaseContext(), new RocketChatDatabaseHelper.DBCallback<Object>() {
                        @Override
                        public Object process(SQLiteDatabase db) throws Exception {
                            s.put(db);
                            return null;
                        }
                    });

                    Log.e(TAG, "websocket: failed to connect.", task.getError());
                    stopSelf();
                }
                else {
                    JSONObject result = task.getResult().result;
                    s.authUserId = result.getString("id");
                    s.authToken = result.getString("token");
                    s.syncstate = SyncState.SYNCED;
                    s.passwd = "";

                    RocketChatDatabaseHelper.write(getBaseContext(), new RocketChatDatabaseHelper.DBCallback<Object>() {
                        @Override
                        public Object process(SQLiteDatabase db) throws Exception {
                            s.put(db);
                            return null;
                        }
                    });

                    registerListeners();
                }
                return null;
            }
        });
    }

    private static final Class[] HANDLER_CLASSES = {

    };
    private final ArrayList<AbstractObserver> mObservers = new ArrayList<AbstractObserver>();

    private void registerObservers() {
        final Context context = getApplicationContext();
        for(Class clazz: HANDLER_CLASSES){
            try {
                Constructor ctor = clazz.getConstructor(Context.class);
                Object obj = ctor.newInstance(context);

                AbstractObserver observer = (AbstractObserver) obj;
                observer.register();
                mObservers.add(observer);
            } catch (Exception e) {
            }
        }
    }

    private void unregisterObservers() {
        Iterator<AbstractObserver> it = mObservers.iterator();
        while(it.hasNext()){
            AbstractObserver observer = it.next();
            observer.unregister();
            it.remove();
        }
    }

    private void registerListeners(){
        //DB observer
        registerObservers();

        //DDP callback observer
//        mAPI.sub();
//        mAPI.sub();
//        mAPI.sub();
//        mAPI.sub();
//            :
//            :
//
    }

    @Override
    public void onDestroy() {
        unregisterObservers();;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
