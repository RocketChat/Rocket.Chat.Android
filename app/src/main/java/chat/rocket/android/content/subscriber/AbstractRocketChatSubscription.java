package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

abstract class AbstractRocketChatSubscription extends AbstractSubscriber {

    private String mID;
    private Subscription mSubscription;

    public AbstractRocketChatSubscription(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    protected abstract String getSubscriptionName();
    protected abstract String getSubscriptionCallbackName();
    protected abstract void onDocumentAdded(DDPSubscription.Added docEvent) throws JSONException;
    protected void onDocumentChanged(DDPSubscription.Changed docEvent) throws JSONException{}

    @Override
    protected void onSubscribe() {
        mAPI.subscribe(getSubscriptionName(),null).onSuccess(new Continuation<DDPSubscription.Ready, Object>() {
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
                                && getSubscriptionCallbackName().equals(((DDPSubscription.DocEvent) event).collection);
                    }
                })
                .cast(DDPSubscription.DocEvent.class)
                .subscribe(new Action1<DDPSubscription.DocEvent>() {
                    @Override
                    public void call(DDPSubscription.DocEvent docEvent) {
                        try {
                            if (docEvent instanceof DDPSubscription.Added.Before) {

                            } else if (docEvent instanceof DDPSubscription.Added) {
                                onDocumentAdded((DDPSubscription.Added) docEvent);
                            } else if (docEvent instanceof DDPSubscription.Removed) {
                            } else if (docEvent instanceof DDPSubscription.Changed) {
                                onDocumentChanged((DDPSubscription.Changed)docEvent);
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
