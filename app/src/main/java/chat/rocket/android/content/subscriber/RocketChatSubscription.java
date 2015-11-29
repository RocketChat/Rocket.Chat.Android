package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.HashMap;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.RocketChatDocument;
import chat.rocket.android.model.Room;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class RocketChatSubscription extends AbstractSubscriber {

    private HashMap<String, RocketChatDocument> mDocumentStore;
    private String mID;

    public RocketChatSubscription(Context context, RocketChatWSAPI api) {
        super(context, api);
    }

    @Override
    protected void onSubscribe() {
        mDocumentStore = new HashMap<String, RocketChatDocument>();
        mAPI.subscribe("subscription",null).onSuccess(new Continuation<DDPSubscription.Ready, Object>() {
            @Override
            public Object then(Task<DDPSubscription.Ready> task) throws Exception {
                mID = task.getResult().id;

                return null;
            }
        }).continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                if(task.isFaulted()){
                    Log.e(TAG, "error", task.getError());
                }
                return null;
            }
        });

        registerSubscriptionCallback();
    }

    private void registerSubscriptionCallback(){
        mAPI.getSubscriptionCallback()
                .filter(new Func1<DDPSubscription.Event, Boolean>() {
                    @Override
                    public Boolean call(DDPSubscription.Event event) {
                        return event instanceof DDPSubscription.DocEvent
                                && "rocketchat_subscription".equals(((DDPSubscription.DocEvent) event).collection);
                    }
                })
                .cast(DDPSubscription.DocEvent.class)
                .subscribe(new Action1<DDPSubscription.DocEvent>() {
                    @Override
                    public void call(DDPSubscription.DocEvent docEvent) {
                        try {
                            if (docEvent instanceof DDPSubscription.Added.Before) {

                            } else if (docEvent instanceof DDPSubscription.Added) {
                                final String roomID = ((DDPSubscription.Added) docEvent).fields.getString("rid");
                                String roomName = ((DDPSubscription.Added) docEvent).fields.getString("name");
                                long roomTs = ((DDPSubscription.Added) docEvent).fields.getJSONObject("ts").getLong("$date");

                                mDocumentStore.put(docEvent.docID, new RocketChatDocument(docEvent.docID, Room.class, roomID));

                                Room r = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<Room>() {
                                    @Override
                                    public Room process(SQLiteDatabase db) throws Exception {
                                        return Room.getById(db, roomID);
                                    }
                                });
                                if(r==null) {
                                    r = new Room();
                                    r.id = roomID;
                                }
                                r.name = roomName;
                                r.timestamp = roomTs;
                                r.putByContentProvider(mContext);
                            } else if (docEvent instanceof DDPSubscription.Removed) {

                            } else if (docEvent instanceof DDPSubscription.Changed) {

                            } else if (docEvent instanceof DDPSubscription.MovedBefore) {

                            }
                        } catch (Exception e) {
                            Log.e(TAG, "error", e);
                        }

                    }
                });

    }

    @Override
    protected void onUnsubscribe() {
        if(!TextUtils.isEmpty(mID)) {
            mAPI.unsubscribe(mID).continueWith(new Continuation<DDPSubscription.NoSub, Object>() {
                @Override
                public Object then(Task<DDPSubscription.NoSub> task) throws Exception {
                    return null;
                }
            });
        }
    }
}
