package chat.rocket.android.content.observer;

import android.annotation.TargetApi;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import chat.rocket.android.Constants;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.Registerable;

abstract class AbstractObserver extends ContentObserver implements Registerable {
    protected static final String TAG = Constants.LOG_TAG;
    protected final Context mContext;
    protected final RocketChatWSAPI mAPI;
    private final Handler mHandler;

    public AbstractObserver(Context context, Looper looper, RocketChatWSAPI api) {
        super(null);
        mContext = context;
        mHandler = new Handler(looper);
        mAPI = api;
    }

    @Override
    public void register(){
        mContext.getContentResolver().registerContentObserver(getTargetUri(), true, this);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onCreate(getTargetUri());
            }
        });
    }

    @Override
    public void unregister(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onDestroy();
            }
        });
        mContext.getContentResolver().unregisterContentObserver(this);
    }



    public abstract Uri getTargetUri();

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        onChange(uri);
    }

    protected void onCreate(Uri uri){}
    protected abstract void onChange(Uri uri);
    protected void onDestroy(){}
}
