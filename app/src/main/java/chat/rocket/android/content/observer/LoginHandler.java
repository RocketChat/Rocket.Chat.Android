package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.SyncState;
import hugo.weaving.DebugLog;
import jp.co.crowdworks.android_ddp.ddp.DDPClientCallback;

public class LoginHandler extends AbstractObserver {

    public LoginHandler(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    public Uri getTargetUri() {
        return RocketChatProvider.getUriForQuery(ServerConfig.TABLE_NAME);
    }

    @Override
    protected void onCreate(Uri uri) {
        super.onCreate(uri);
        Cursor c = mContext.getContentResolver().query(uri,null,"is_primary = 1 AND syncstate!=2",null,null);
        if (c!=null && c.getCount()>0 && c.moveToFirst()) handleLogin(c);
    }

    @Override
    public void onChange(Uri uri) {
        Cursor c = mContext.getContentResolver().query(uri,null,"is_primary = 1 AND syncstate=0",null,null);
        if (c!=null && c.getCount()>0 && c.moveToFirst()) handleLogin(c);
    }

    @DebugLog
    private void handleLogin(Cursor c) {
        final ServerConfig s = ServerConfig.createFromCursor(c);

        s.syncstate = SyncState.SYNCING;
        s.putByContentProvider(mContext);

        try {
            login(s).onSuccess(new Continuation<DDPClientCallback.RPC, Object>() {
                @Override
                public Object then(Task<DDPClientCallback.RPC> task) throws Exception {
                    JSONObject result = task.getResult().result;
                    s.authUserId = result.getString("id");
                    s.authToken = result.getString("token");
                    s.syncstate = SyncState.SYNCED;
                    s.passwd = "";
                    s.putByContentProvider(mContext);

                    return null;
                }
            }).continueWith(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {
                    if(task.isFaulted()){
                        if(task.getError() instanceof DDPClientCallback.RPC.Error){
                            s.deleteByContentProvider(mContext);
                        }
                        else {
                            s.syncstate = SyncState.FAILED;
                            s.putByContentProvider(mContext);
                        }
                    }

                    return null;
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "error", e);
        }
    }

    private Task<DDPClientCallback.RPC> login(ServerConfig s) throws JSONException{
        if (!TextUtils.isEmpty(s.authToken)) {
            return mAPI.login(s.authToken);
        } else {
            return mAPI.login(s.account, s.passwd);
        }
    }
}
