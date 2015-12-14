package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;

import org.json.JSONException;

import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.User;
import chat.rocket.android.preference.Cache;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;

public class UserData extends AbstractRocketChatSubscription {
    public UserData(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    protected String getSubscriptionName() {
        return "userData";
    }

    @Override
    protected String getSubscriptionCallbackName() {
        return "users";
    }

    @Override
    protected void onDocumentAdded(DDPSubscription.Added docEvent) throws JSONException {
        final String userId = docEvent.docID;

        User u = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<User>() {
            @Override
            public User process(SQLiteDatabase db) throws Exception {
                return User.getById(db, userId);
            }
        });
        if(u==null) {
            u = new User();
            u.id = userId;
        }
        u.name = docEvent.fields.getString("username");
        u.syncstate = SyncState.SYNCED;
        if(!docEvent.fields.isNull("status")) {
            u.status = User.Status.getType(docEvent.fields.getString("status"));
        }
        u.putByContentProvider(mContext);

        final boolean isMe = (!docEvent.fields.isNull("emails"));
        if(isMe) {
            Cache.get(mContext).edit()
                    .putString(Cache.KEY_MY_USER_ID, u.id)
                    .putString(Cache.KEY_MY_USER_NAME, u.name)
                    .commit();
        }
    }

    @Override
    protected void onDocumentChanged(DDPSubscription.Changed docEvent) throws JSONException {
        final String userId = docEvent.docID;
        User u = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<User>() {
            @Override
            public User process(SQLiteDatabase db) throws Exception {
                return User.getById(db, userId);
            }
        });
        if (u!=null && !docEvent.fields.isNull("status")) {
            u.status = User.Status.getType(docEvent.fields.getString("status"));
            u.syncstate = SyncState.SYNCED;
            u.putByContentProvider(mContext);
        }
    }
}
