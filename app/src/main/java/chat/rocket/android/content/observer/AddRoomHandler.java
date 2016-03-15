package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.SyncState;
import chat.rocket.android_ddp.DDPClientCallback;
import hugo.weaving.DebugLog;

public class AddRoomHandler extends AbstractObserver {

    public AddRoomHandler(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    public Uri getTargetUri() {
        return RocketChatProvider.getUriForQuery(Room.TABLE_NAME);
    }

    @Override
    protected void onCreate(Uri uri) {
        super.onCreate(uri);
        Cursor c = mContext.getContentResolver().query(uri,null,"syncstate!=2 AND id='DUMMY' AND (name IS NOT NULL) AND (type IS NOT NULL)",null,null);
        if (c==null) return;
        if (c.getCount()>0 && c.moveToFirst()) handleAddRoom(c);
        c.close();
    }

    @Override
    public void onChange(Uri uri) {
        Cursor c = mContext.getContentResolver().query(uri,null,"syncstate=0 AND id='DUMMY' AND (name IS NOT NULL) AND (type IS NOT NULL)",null,null);
        if (c==null) return;
        if (c.getCount()>0 && c.moveToFirst()) handleAddRoom(c);
        c.close();
    }

    @DebugLog
    private void handleAddRoom(Cursor c) {
        final Room r = Room.createFromCursor(c);

        r.syncstate = SyncState.SYNCING;
        r.putByContentProvider(mContext);

        try {
            createRoom(r).onSuccess(new Continuation<DDPClientCallback.RPC, Object>() {
                @Override
                public Object then(Task<DDPClientCallback.RPC> task) throws Exception {
                    JSONObject result = task.getResult().result;

                    // room ID is notified also via "rocketchat_subscription / added"
                    //  and it is handled by RocketChatSubscription subscriber.
                    // So just remove DUMMY room obj here.

                    r.deleteByContentProvider(mContext);
                    return null;
                }
            }).continueWith(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {
                    if(task.isFaulted()){
                        if(task.getError() instanceof DDPClientCallback.RPC.Error){
                            r.deleteByContentProvider(mContext);
                        }
                        else {
                            r.syncstate = SyncState.FAILED;
                            r.putByContentProvider(mContext);
                        }
                    }

                    return null;
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, "error", e);
        }
    }

    private Task<DDPClientCallback.RPC> createRoom(Room r) throws JSONException{
        switch (r.type) {
            case CHANNEL: return mAPI.createChannel(r.name,new JSONArray());
            case DIRECT_MESSAGE: return mAPI.createDirectMessage(r.name);
            case PRIVATE_GROUP: return mAPI.createPrivateGroup(r.name, new JSONArray());
        }
        return Task.forError(new IllegalArgumentException("invalid room type"));
    }
}
