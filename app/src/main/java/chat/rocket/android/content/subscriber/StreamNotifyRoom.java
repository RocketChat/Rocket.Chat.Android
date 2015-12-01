package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.Message;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;

public class StreamNotifyRoom extends AbstractRocketChatSubscription {

    public StreamNotifyRoom(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    protected String getSubscriptionName() {
        return "stream-notify-room";
    }

    @Override
    protected String getSubscriptionCallbackName() {
        return "stream-notify-room";
    }

    @Override
    protected void onDocumentAdded(DDPSubscription.Added docEvent) throws JSONException {
        if(!docEvent.fields.isNull("args")) {
            final JSONArray args = docEvent.fields.getJSONArray("args");
            final String path = args.getString(0);
            if (!TextUtils.isEmpty(path) && path.endsWith("/deleteMessage")) {
                final JSONObject obj = args.getJSONObject(1);

                final String messageID = obj.getString("_id");
                Message m = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<Message>() {
                    @Override
                    public Message process(SQLiteDatabase db) throws Exception {
                        return Message.getById(db, messageID);
                    }
                });
                if (m != null) m.deleteByContentProvider(mContext);
            }
        }
    }
}
