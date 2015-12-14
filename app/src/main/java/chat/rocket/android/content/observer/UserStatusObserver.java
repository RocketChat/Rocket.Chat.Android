package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.User;
import jp.co.crowdworks.android_ddp.ddp.DDPClientCallback;

public class UserStatusObserver extends AbstractObserver {
    public UserStatusObserver(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    public Uri getTargetUri() {
        return RocketChatProvider.getUriForQuery(User.TABLE_NAME);
    }


    @Override
    protected void onCreate(Uri uri) {
        super.onCreate(uri);
        Cursor c = mContext.getContentResolver().query(uri,null,"syncstate!=2 AND id IS NOT NULL",null,null);
        if (c!=null && c.getCount()>0 && c.moveToFirst()) {
            handleUserStatusChanged(c);
            c.close();
        }
    }

    @Override
    public void onChange(Uri uri) {
        Cursor c = mContext.getContentResolver().query(uri,null,"syncstate=0 AND id IS NOT NULL",null,null);
        if (c!=null && c.getCount()>0 && c.moveToFirst()) {
            handleUserStatusChanged(c);
            c.close();
        }
    }

    private void handleUserStatusChanged(Cursor c) {
        final User u = User.createFromCursor(c);

        u.syncstate = SyncState.SYNCING;
        u.putByContentProvider(mContext);

        mAPI.setUserStatus(u.status).onSuccess(new Continuation<DDPClientCallback.RPC, Object>() {
            @Override
            public Object then(Task<DDPClientCallback.RPC> task) throws Exception {
                u.syncstate = SyncState.SYNCED;
                u.putByContentProvider(mContext);
                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                if(task.isFaulted()){
                    u.syncstate = SyncState.FAILED;
                    u.putByContentProvider(mContext);
                }
                return null;
            }
        });
    }
}
