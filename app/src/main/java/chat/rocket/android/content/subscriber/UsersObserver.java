package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.util.Log;

import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.User;
import chat.rocket.android.preference.Cache;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

public class UsersObserver extends AbstractSubscriber {
    Subscription mSubscription;

    public UsersObserver(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    protected void onSubscribe() {
        mSubscription = mAPI.getSubscriptionCallback()
                .filter(new Func1<DDPSubscription.Event, Boolean>() {
                    @Override
                    public Boolean call(DDPSubscription.Event event) {
                        return event instanceof DDPSubscription.DocEvent &&
                                "users".equals(((DDPSubscription.DocEvent) event).collection);
                    }
                })
                .cast(DDPSubscription.DocEvent.class)
                .subscribe(new Action1<DDPSubscription.DocEvent>() {
                    @Override
                    public void call(DDPSubscription.DocEvent docEvent) {
                        try {
                            if (docEvent instanceof DDPSubscription.Added.Before) {

                            } else if (docEvent instanceof DDPSubscription.Added) {
                                final String username = ((DDPSubscription.Added) docEvent).fields.getString("username");

                                User u = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<User>() {
                                    @Override
                                    public User process(SQLiteDatabase db) throws Exception {
                                        return User.getById(db, username);
                                    }
                                });
                                if(u==null) {
                                    u = new User();
                                    u.id = username;
                                    u.putByContentProvider(mContext);
                                }

                                final boolean isMe = (!((DDPSubscription.Added) docEvent).fields.isNull("emails"));
                                if(isMe) {
                                    Cache.get(mContext).edit()
                                            .putString(Cache.KEY_MY_USER_ID, u.id)
                                            .commit();
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
    protected void onUnsubscribe() {
        mSubscription.unsubscribe();
    }
}
