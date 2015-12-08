package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.SyncState;
import jp.co.crowdworks.android_ddp.ddp.DDPClientCallback;

public class MarkRoomAsReadHandler extends AbstractObserver {

    public MarkRoomAsReadHandler(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    public Uri getTargetUri() {
        return RocketChatProvider.getUriForQuery(Room.TABLE_NAME);
    }

    @Override
    protected void onCreate(Uri uri) {
        super.onCreate(uri);
        Cursor c = mContext.getContentResolver().query(uri,null,"syncstate!=2 AND alert=0",null,null);
        while(c!=null && c.moveToNext()) markRoomAsRead(c);
    }

    @Override
    protected void onChange(Uri uri) {
        Cursor c = mContext.getContentResolver().query(uri,null,"syncstate=0 AND alert=0",null,null);
        if (c!=null && c.getCount()>0 && c.moveToFirst()) markRoomAsRead(c);
    }

    private void markRoomAsRead(Cursor c) {
        final Room r = Room.createFromCursor(c);

        r.syncstate = SyncState.SYNCING;
        r.putByContentProvider(mContext);

        try {
            mAPI.markAsRead(r.id).continueWith(new Continuation<DDPClientCallback.RPC, Object>() {
                @Override
                public Object then(Task<DDPClientCallback.RPC> task) throws Exception {
                    JSONObject result = task.getResult().result;
                    if(task.isFaulted()){
                        r.syncstate = SyncState.FAILED;
                        Log.e(TAG,"error",task.getError());
                    }
                    else{
                        r.syncstate = SyncState.SYNCED;
                    }
                    r.putByContentProvider(mContext);
                    return null;
                }
            });
        } catch (JSONException e) {
            Log.e(TAG,"error",e);
        }
    }
}
