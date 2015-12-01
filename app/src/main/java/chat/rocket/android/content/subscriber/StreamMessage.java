package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.JSONParseEngine;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.model.Message;
import chat.rocket.android.model.RocketChatDocument;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class StreamMessage extends AbstractSubscriber {

    private HashMap<String, RocketChatDocument> mDocumentStore;
    private String mID;
    private Subscription mSubscription;
    private final JSONParseEngine mParser;

    public StreamMessage(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
        mParser = new JSONParseEngine(context);
    }


    @Override
    protected void onSubscribe() {
        mDocumentStore = new HashMap<String, RocketChatDocument>();
        mAPI.subscribe("stream-messages",null).onSuccess(new Continuation<DDPSubscription.Ready, Object>() {
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
        mSubscription = mAPI.getSubscriptionCallback()
                .filter(new Func1<DDPSubscription.Event, Boolean>() {
                    @Override
                    public Boolean call(DDPSubscription.Event event) {
                        return event instanceof DDPSubscription.DocEvent
                                && "stream-messages".equals(((DDPSubscription.DocEvent) event).collection);
                    }
                })
                .cast(DDPSubscription.DocEvent.class)
                .subscribe(new Action1<DDPSubscription.DocEvent>() {
                    @Override
                    public void call(DDPSubscription.DocEvent docEvent) {
                        try {
                            if (docEvent instanceof DDPSubscription.Added.Before) {

                            } else if (docEvent instanceof DDPSubscription.Added) {
                                final JSONArray args = ((DDPSubscription.Added) docEvent).fields.getJSONArray("args");
                                final String roomID = args.getString(0);
                                final JSONObject message = args.getJSONObject(1);
                                final String messageID = message.getString("_id");

                                mParser.parseMessage(message);

                                mDocumentStore.put(docEvent.docID, new RocketChatDocument(docEvent.docID, Message.class, messageID));
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
