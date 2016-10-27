package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.JSONParseEngine;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Room;
import chat.rocket.android.model.UserRoom;
import chat.rocket.android_ddp.DDPSubscription;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class RocketChatRoom extends AbstractObserver {

    private String mID;
    private Subscription mSubscription;

    private JSONParseEngine parseEngine;

    public RocketChatRoom(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);

        parseEngine = new JSONParseEngine(context);
    }

    @Override
    public Uri getTargetUri() {
        return RocketChatProvider.getUriForQuery(Room.TABLE_NAME);
    }


    @Override
    protected void onCreate(Uri uri) {
        super.onCreate(uri);
        registerSubscriptionCallback();
    }

    @Override
    protected void onChange(Uri uri) {
        if (mID != null) {
            mAPI.unsubscribe(mID);
        }

        Cursor c = mContext.getContentResolver().query(uri, null, "id!='DUMMY' AND syncstate=2", null, null);
        if (c == null) return;
        if (c.getCount() == 0) {
            //removed
        } else if (c.moveToFirst()) {
            //added or updated
            Room r = Room.createFromCursor(c);

            JSONArray params = new JSONArray().put(r.rid);

            mAPI.subscribe("stream-room-messages", params).onSuccess(new Continuation<DDPSubscription.Ready, Object>() {
                @Override
                public Object then(Task<DDPSubscription.Ready> task) throws Exception {
                    mID = task.getResult().id;

                    return null;
                }
            });
        }
        c.close();
    }

    private void registerSubscriptionCallback() {
        mSubscription = mAPI.getSubscriptionCallback()
                .filter(new Func1<DDPSubscription.Event, Boolean>() {
                    @Override
                    public Boolean call(DDPSubscription.Event event) {
                        return event instanceof DDPSubscription.DocEvent
                                && "stream-room-messages".equals(((DDPSubscription.DocEvent) event).collection);
                    }
                })
                .cast(DDPSubscription.DocEvent.class)
                .subscribe(new Action1<DDPSubscription.DocEvent>() {
                    @Override
                    public void call(DDPSubscription.DocEvent docEvent) {
                        try {
                            if (docEvent instanceof DDPSubscription.Added.Before) {

                            } else if (docEvent instanceof DDPSubscription.Added) {
                                final String roomID = docEvent.docID;
                                final JSONArray usernames = ((DDPSubscription.Added) docEvent).fields.getJSONArray("usernames");
                                RocketChatDatabaseHelper.writeWithTransaction(mContext, new RocketChatDatabaseHelper.DBCallbackEx<Object>() {
                                    @Override
                                    public Object process(SQLiteDatabase db) throws Exception {
                                        UserRoom.delete(db, "room_id=?", new String[]{roomID});
                                        for (int i = 0; i < usernames.length(); i++) {
                                            String username = usernames.getString(i);
                                            UserRoom userRoom = new UserRoom();
                                            userRoom.username = username;
                                            userRoom.roomID = roomID;
                                            userRoom.put(db);
                                        }

                                        return null;
                                    }

                                    @Override
                                    public void handleException(Exception e) {
                                        Log.e(TAG, "error", e);
                                    }
                                });
                            } else if (docEvent instanceof DDPSubscription.Removed) {
                            } else if (docEvent instanceof DDPSubscription.Changed) {
                                final JSONArray args = ((DDPSubscription.Changed) docEvent).fields.getJSONArray("args");

                                RocketChatDatabaseHelper.writeWithTransaction(mContext, new RocketChatDatabaseHelper.DBCallbackEx<Object>() {
                                    @Override
                                    public Object process(SQLiteDatabase db) throws Exception {
                                        for (int i = 0, size = args.length(); i < size; i++) {
                                            parseEngine.parseMessage((JSONObject) args.get(i), db);
                                        }

                                        return null;
                                    }

                                    @Override
                                    public void handleException(Exception e) {
                                        Log.e(TAG, "error", e);
                                    }
                                });

                            } else if (docEvent instanceof DDPSubscription.MovedBefore) {

                            }
                        } catch (Exception e) {
                            Log.e(TAG, "error", e);
                        }

                    }
                });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mSubscription != null) mSubscription.unsubscribe();

        if (!TextUtils.isEmpty(mID)) {
            mAPI.unsubscribe(mID).continueWith(new Continuation<DDPSubscription.NoSub, Object>() {
                @Override
                public Object then(Task<DDPSubscription.NoSub> task) throws Exception {
                    return null;
                }
            });
        }
    }
}
