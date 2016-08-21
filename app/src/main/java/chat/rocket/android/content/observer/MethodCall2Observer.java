package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.Constants;
import chat.rocket.android.api.JSONParseEngine;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.api.ws.RocketChatWSService;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.helper.MyRealmHelper;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.Room;
import chat.rocket.android.model2.MethodCall;
import chat.rocket.android.model2.SyncState;
import chat.rocket.android_ddp.DDPClientCallback;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmQuery;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

/**
 */
public class MethodCall2Observer extends AbstractRealmObserver<MethodCall> {


    public MethodCall2Observer(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @DebugLog
    private Task<DDPClientCallback.RPC> getMethod(final String id, final String op, JSONObject params) throws JSONException {
        if("loadMessages".equals(op)) {
            return mAPI.loadMessages(params.getString("room_id"), params.optLong("end_ts",-1), params.optInt("num",50));
        }
        else if("logout".equals(op)) {
            return mAPI.logout();
        }
        else if("uploadFile".equals(op)) {
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
                        public void onProgress(final long sent, final long total) {
                            RealmHelperBolts.executeTransactionAsync(new RealmHelperBolts.Transaction() {
                                @Override
                                public Object execute(Realm realm) throws Exception {
                                    realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
                                            .put("id", id)
                                            .put("returns", new JSONObject()
                                                    .put("sent", sent)
                                                    .put("total", total).toString())
                                    );
                                    return null;
                                }
                            }).continueWith(Constants.ERROR_LOGGING);
                        }
                    });
        }
        else if("rooms/get".equals(op)) {
            return mAPI.getRooms(params.getLong("timestamp"));
        }

        throw new IllegalArgumentException("op("+op+") is not known.");
    }

    @Override
    protected RealmQuery<MethodCall> query(Realm realm) {
        return realm.where(MethodCall.class).equalTo("syncstate", SyncState.NOT_SYNCED);
    }

    @Override
    protected void onModelChanged(MethodCall methodCall) {
        handleMethodCall(methodCall).continueWith(Constants.ERROR_LOGGING);
    }

    private Task<Void> handleMethodCall(MethodCall methodCall) {
        final String id = methodCall.getId();
        final String op = methodCall.getOp();
        final JSONObject params;
        try {
            params = new JSONObject(methodCall.getParams());
        } catch (JSONException e) {
            return removeItem(id);
        }

        return RealmHelperBolts.executeTransactionAsync(new RealmHelperBolts.Transaction() {
            @Override
            public Object execute(Realm realm) throws Exception {
                return realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
                        .put("id", id)
                        .put("syncstate", SyncState.SYNCING)
                        .put("timestamp", System.currentTimeMillis()));
            }
        }).onSuccessTask(new Continuation<Void, Task<DDPClientCallback.RPC>>() {
            @Override
            public Task<DDPClientCallback.RPC> then(Task<Void> task) throws Exception {
                return getMethod(id, op, params);
            }
        }).onSuccessTask(new Continuation<DDPClientCallback.RPC, Task<Void>>() {
            @Override
            public Task<Void> then(Task<DDPClientCallback.RPC> task) throws Exception {
                final JSONObject result = task.getResult().result;
                ResultHandler handler = getResultHandler(op, params);
                if(handler == null || !handler.handleResult(result)) {
                    return MyRealmHelper.executeTransaction(new RealmHelperBolts.Transaction<Void>() {
                        @Override
                        public Void execute(Realm realm) throws Exception {
                            realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
                                    .put("id", id)
                                    .put("syncstate", SyncState.SYNCED)
                                    .put("timestamp", System.currentTimeMillis())
                                    .put("returns", result.toString()));
                            return null;
                        }
                    });
                }
                else {
                    return removeItem(id);
                }
            }
        });
    }

    private Task<Void> removeItem(final String id) {
        return MyRealmHelper.executeTransaction(new RealmHelperBolts.Transaction() {
            @Override
            public Object execute(Realm realm) throws Exception {
                return realm.where(MethodCall.class).equalTo("id", id).findAll().deleteAllFromRealm();
            }
        });
    }

    private interface ResultHandler {
        boolean handleResult(JSONObject result) throws Exception;
    }

    @DebugLog
    private ResultHandler getResultHandler(String op, JSONObject params) throws JSONException {
        if("loadMessages".equals(op)) {
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
        else if ("logout".equals(op)) {
            return new ResultHandler() {
                @Override
                public boolean handleResult(JSONObject result) throws Exception {
                    RocketChatWSService.kill(mContext);
                    return true;
                }
            };
        }
        else if ("rooms/get".equals(op)) {
            return new ResultHandler() {
                @Override
                public boolean handleResult(JSONObject result) throws Exception {
                    JSONParseEngine parser = new JSONParseEngine(mContext);
                    JSONArray roomsToUpdate = result.getJSONArray("update");
                    for (int i=0; i<roomsToUpdate.length(); i++) {
                        JSONObject room = roomsToUpdate.getJSONObject(i);
                        room.put("rid", room.getString("_id"));
                        parser.parseRoom(room);
                    }


                    JSONArray roomsToRemove = result.getJSONArray("remove");
                    for (int i=0; i<roomsToRemove.length(); i++) {
                        JSONObject room = roomsToRemove.getJSONObject(i);
                        final String id = room.getString("_id");
                        Room r = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<Room>() {
                            @Override
                            public Room process(SQLiteDatabase db) throws Exception {
                                return Room.getById(db, id);
                            }
                        });
                        if (r!=null) r.deleteByContentProvider(mContext);
                    }
                    return true;
                }
            };
        }
        return null;
    }
}
