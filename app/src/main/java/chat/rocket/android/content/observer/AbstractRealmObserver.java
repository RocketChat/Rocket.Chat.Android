package chat.rocket.android.content.observer;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import chat.rocket.android.Constants;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.Registerable;
import io.realm.RealmObject;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;

public abstract class AbstractRealmObserver<T extends RealmObject> extends RealmObjectObserver<T> implements Registerable {
    protected static final String TAG = Constants.LOG_TAG;
    protected final Context mContext;
    protected final RocketChatWSAPI mAPI;
    private final Handler mHandler;

    public AbstractRealmObserver(Context context, Looper looper, RocketChatWSAPI api) {
        super();
        mContext = context;
        mHandler = new Handler(looper);
        mAPI = api;
    }

    @Override
    public void register() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                sub();
            }
        });
    }

    @Override
    public void unregister() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                unsub();
            }
        });
    }

    @Override
    protected final void onChange(final T t) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onModelChanged(t);
            }
        });
    }

    protected abstract void onModelChanged(T t);
}