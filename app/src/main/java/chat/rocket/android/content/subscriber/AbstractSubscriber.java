package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import chat.rocket.android.Constants;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.Registerable;

abstract class AbstractSubscriber implements Registerable {
    protected static final String TAG = Constants.LOG_TAG;
    protected final Context mContext;
    protected final RocketChatWSAPI mAPI;
    private final Handler mHandler;

    public AbstractSubscriber(Context context, Looper looper, RocketChatWSAPI api) {
        mContext = context;
        mHandler = new Handler(looper);
        mAPI = api;
    }

    @Override
    public void register(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onSubscribe();
            }
        });
    }

    @Override
    public void unregister(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onUnsubscribe();
            }
        });
    }

    protected abstract void onSubscribe();
    protected abstract void onUnsubscribe();
}
