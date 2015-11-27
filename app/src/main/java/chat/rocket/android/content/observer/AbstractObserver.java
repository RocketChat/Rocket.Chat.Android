package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;

public abstract class AbstractObserver extends ContentObserver {
    protected final Context mContext;
    public AbstractObserver(Context context) {
        super(null);
        mContext = context;
    }

    public void register(){
        mContext.getContentResolver().registerContentObserver(getTargetUri(), true, this);
    }

    public void unregister(){
        mContext.getContentResolver().unregisterContentObserver(this);
    }

    public abstract Uri getTargetUri();
}
