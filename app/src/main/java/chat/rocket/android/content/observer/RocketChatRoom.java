package chat.rocket.android.content.observer;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatProvider;
import chat.rocket.android.model.Room;
import hugo.weaving.DebugLog;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class RocketChatRoom extends AbstractObserver {

    private String mID;
    private Subscription mSubscription;

    public RocketChatRoom(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
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

    @DebugLog
    @Override
    protected void onChange(Uri uri) {
        Cursor c = mContext.getContentResolver().query(uri, null, null, null, null);
        if(c==null || c.getCount()==0) {
            //removed
        }
        else if(c.moveToFirst()){
            //added or updated
            Room r = Room.createFromCursor(c);

            JSONArray params = new JSONArray().put("c"+r.name);

            mAPI.subscribe("room",params).onSuccess(new Continuation<DDPSubscription.Ready, Object>() {
                @Override
                public Object then(Task<DDPSubscription.Ready> task) throws Exception {
                    mID = task.getResult().id;

                    return null;
                }
            });
        }
    }

    private void registerSubscriptionCallback(){
        mSubscription = mAPI.getSubscriptionCallback()
                .filter(new Func1<DDPSubscription.Event, Boolean>() {
                    @Override
                    public Boolean call(DDPSubscription.Event event) {
                        return event instanceof DDPSubscription.DocEvent
                                && "rocketchat_room".equals(((DDPSubscription.DocEvent) event).collection);
                    }
                })
                .cast(DDPSubscription.DocEvent.class)
                .subscribe(new Action1<DDPSubscription.DocEvent>() {
                    @Override
                    public void call(DDPSubscription.DocEvent docEvent) {
                        try {
                            if (docEvent instanceof DDPSubscription.Added.Before) {

                            } else if (docEvent instanceof DDPSubscription.Added) {
                                String roomID = docEvent.docID;
                                JSONArray usernames = ((DDPSubscription.Added) docEvent).fields.getJSONArray("usernames");
                                for(int i=0;i<usernames.length();i++){
                                    String username = usernames.getString(i);
                                    Log.d(TAG, "[room:"+roomID+"] <- "+username);
                                }
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
    protected void onDestroy() {
        super.onDestroy();

        if(mSubscription!=null) mSubscription.unsubscribe();

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
