package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;

import org.json.JSONException;

import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.Room;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;

public class RocketChatSubscription extends AbstractRocketChatSubscription {

    public RocketChatSubscription(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    protected String getSubscriptionName() {
        return "subscription";
    }

    @Override
    protected String getSubscriptionCallbackName() {
        return "rocketchat_subscription";
    }

    @Override
    protected void onDocumentAdded(DDPSubscription.DocEvent docEvent) throws JSONException {
        final String roomID = ((DDPSubscription.Added) docEvent).fields.getString("rid");
        String roomName = ((DDPSubscription.Added) docEvent).fields.getString("name");
        long roomTs = ((DDPSubscription.Added) docEvent).fields.getJSONObject("ts").getLong("$date");

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
    }
}
