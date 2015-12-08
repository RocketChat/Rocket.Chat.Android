package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.JSONParseEngine;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.api.ws.RocketChatWSService;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.SyncState;
import hugo.weaving.DebugLog;
import jp.co.crowdworks.android_ddp.ddp.DDPClientCallback;

public class MethodCallObserver extends AbstractObserver {

    public MethodCallObserver(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    public Uri getTargetUri() {
        return RocketChatProvider.getUriForQuery(MethodCall.TABLE_NAME);
    }

    @Override
    protected void onCreate(Uri uri) {
        super.onCreate(uri);
        Cursor c = mContext.getContentResolver().query(uri,null,"syncstate!=2",null,null);
        while(c!=null && c.moveToNext()) handleMethod(c);
    }

    @Override
    protected void onChange(Uri uri) {
        Cursor c = mContext.getContentResolver().query(uri,null,"syncstate=0",null,null);
        if (c!=null && c.getCount()>0 && c.moveToFirst()) handleMethod(c);
    }

    private void handleMethod(Cursor c) {
        final MethodCall m = MethodCall.createFromCursor(c);
        m.syncstate = SyncState.SYNCING;
        m.timestamp = System.currentTimeMillis();
        m.putByContentProvider(mContext);

        try {
            final JSONObject params = new JSONObject(m.params);
            getMethod(m, params).onSuccess(new Continuation<DDPClientCallback.RPC, Object>() {
                @Override
                public Object then(Task<DDPClientCallback.RPC> task) throws Exception {
                    JSONObject result = task.getResult().result;
                    ResultHandler handler = getResultHandler(m, params);
                    if(handler == null || !handler.handleResult(result)) {
                        m.returns = result.toString();
                        m.timestamp = System.currentTimeMillis();
                        m.syncstate = SyncState.SYNCED;
                        m.putByContentProvider(mContext);
                    }
                    else {
                        m.deleteByContentProvider(mContext);
                    }
                    return null;
                }
            }).continueWith(new Continuation<Object, Object>() {
                @Override
                public Object then(Task<Object> task) throws Exception {
                    if(task.isFaulted()){
                        Exception e = task.getError();
                        Log.e(TAG, "error", task.getError());
                        if (e instanceof JSONException || e instanceof IllegalArgumentException) {
                            m.deleteByContentProvider(mContext);
                        }
                        else {
                            m.syncstate = SyncState.FAILED;
                            m.putByContentProvider(mContext);
                        }
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "error",e);
            m.deleteByContentProvider(mContext);
        }
    }

    @DebugLog
    private Task<DDPClientCallback.RPC> getMethod(final MethodCall m, JSONObject params) throws JSONException {
        String id = m.id;
        if("loadMessages".equals(id)) {
            return mAPI.loadMessages(params.getString("room_id"), params.optLong("end_ts",-1), params.optInt("num",50));
        }
        else if("logout".equals(id)) {
            return mAPI.logout();
        }
        else if("uploadFile".equals(id)) {
            final Uri localFile = Uri.parse(params.getString("file_uri"));
            final String filename = params.getString("filename");
            return mAPI.uploadFile(mContext,
                    params.getString("room_id"),
                    params.getString("user_id"),
                    filename,
                    localFile,
                    params.getString("mime_type"),
                    params.getLong("file_size"), new RocketChatWSAPI.UploadFileProgress() {
                        @Override
                        public void onProgress(long sent, long total) {
                            try {
                                m.returns = new JSONObject()
                                        .put("sent", sent)
                                        .put("total", total).toString();
                                m.putByContentProvider(mContext);
                            }
                            catch (JSONException e){ }
                        }
                    });
        }

        throw new IllegalArgumentException("id("+id+") is not known.");
    }

    private interface ResultHandler {
        boolean handleResult(JSONObject result) throws Exception;
    }

    @DebugLog
    private ResultHandler getResultHandler(MethodCall m, JSONObject params) throws JSONException {
        String id = m.id;
        if("loadMessages".equals(id)) {
            final String roomId = params.getString("room_id");
            final boolean clearAllMessage = params.optBoolean("clean", false);
            final int num = params.optInt("num",50);
            return new ResultHandler() {
                @Override
                public boolean handleResult(JSONObject result) throws Exception {
                    Log.d(TAG,"unreadNotLoaded="+result.getInt("unreadNotLoaded"));
                    if(clearAllMessage) Message.deleteByContentProvider(mContext, "room_id=?",new String[]{roomId});
                    JSONArray messages = result.getJSONArray("messages");
                    JSONParseEngine parser = new JSONParseEngine(mContext);
                    for(int i=0;i<messages.length();i++) {
                        parser.parseMessage(messages.getJSONObject(i));
                    }

                    Room r = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<Room>() {
                        @Override
                        public Room process(SQLiteDatabase db) throws Exception {
                            return Room.getById(db, roomId);
                        }
                    });
                    if(r!=null) {
                        boolean hasMore = (messages.length() >= num);
                        if(r.hasMore!=hasMore){
                            r.hasMore = hasMore;
                            r.putByContentProvider(mContext);
                        }
                    }

                    return true;
                }
            };
        }
        else if ("logout".equals(id)) {
            return new ResultHandler() {
                @Override
                public boolean handleResult(JSONObject result) throws Exception {
                    RocketChatWSService.kill(mContext);
                    return true;
                }
            };
        }
        return null;
    }
}
