package chat.rocket.android.content.subscriber;

import android.content.Context;

import chat.rocket.android.Constants;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.Registerable;

abstract class AbstractSubscriber implements Registerable {
    protected static final String TAG = Constants.LOG_TAG;
    protected final Context mContext;
    protected final RocketChatWSAPI mAPI;

    public AbstractSubscriber(Context context, RocketChatWSAPI api) {
        mContext = context;
        mAPI = api;
    }

    @Override
    public void register(){
        onSubscribe();
    }

    @Override
    public void unregister(){
        onUnsubscribe();
    }

    protected abstract void onSubscribe();
    protected abstract void onUnsubscribe();
}
