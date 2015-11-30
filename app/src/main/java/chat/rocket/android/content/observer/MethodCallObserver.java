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
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.User;
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
            getMethod(m.id, new JSONObject(m.params)).onSuccess(new Continuation<DDPClientCallback.RPC, Object>() {
                @Override
                public Object then(Task<DDPClientCallback.RPC> task) throws Exception {
                    JSONObject result = task.getResult().result;
                    ResultHandler handler = getResultHandler(m.id);
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
    private Task<DDPClientCallback.RPC> getMethod(String id, JSONObject params) throws JSONException {
        if("loadMessages".equals(id)) {
            return mAPI.loadMessages(params.getString("room_id"), params.optLong("end_ts",-1), params.optInt("num",50));
        }
        else if("logout".equals(id)) {
            return mAPI.logout();
        }

        throw new IllegalArgumentException("id("+id+") is not known.");
    }

    private interface ResultHandler {
        boolean handleResult(JSONObject result) throws Exception;
    }

    @DebugLog
    private ResultHandler getResultHandler(String id) {
        if("loadMessages".equals(id)) {
            return new ResultHandler() {
                @Override
                public boolean handleResult(JSONObject result) throws Exception {
                    Log.d(TAG,"unreadNotLoaded="+result.getInt("unreadNotLoaded"));
                    JSONArray messages = result.getJSONArray("messages");
                    for(int i=0;i<messages.length();i++) {
                        JSONObject message = messages.getJSONObject(i);

                        //"_id":"X6TG3j4pNGA6HBu8Q","rid":"PTKTpXLoo9XTF62ij","msg":"hogehoge","ts":{"$date":1448132287372},"u":{"_id":"vdsT864GD3CkZPr5K","username":"test.user.2-1"}}
                        final String messageId = message.getString("_id");
                        Message m = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<Message>() {
                            @Override
                            public Message process(SQLiteDatabase db) throws Exception {
                                return Message.getById(db, messageId);
                            }
                        });
                        if(m==null){
                            m = new Message();
                            m.id = messageId;
                        }
                        m.roomId = message.getString("rid");
                        m.content = message.getString("msg");
                        m.timestamp = message.getJSONObject("ts").getLong("$date");

                        final JSONObject user = message.getJSONObject("u");
                        final String userId = user.getString("_id");
                        User _u = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<User>() {
                            @Override
                            public User process(SQLiteDatabase db) throws Exception {
                                return User.getById(db, userId);
                            }
                        });
                        if (_u==null) {
                            final User u = new User();
                            u.id = userId;
                            u.name = user.getString("username");
                            RocketChatDatabaseHelper.write(mContext, new RocketChatDatabaseHelper.DBCallback<Object>() {
                                @Override
                                public Object process(SQLiteDatabase db) throws Exception {
                                    u.put(db);
                                    return null;
                                }
                            });
                        }

                        m.userId = userId;
                        m.putByContentProvider(mContext);
                    }

                    return true;
                }
            };
        }
        else if ("logout".equals(id)) {
            return new ResultHandler() {
                @Override
                public boolean handleResult(JSONObject result) throws Exception {
                    return true;
                }
            };
        }
        return null;
    }
}
