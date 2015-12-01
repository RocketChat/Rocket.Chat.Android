package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.api.JSONParseEngine;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;

public class StreamMessage extends AbstractRocketChatSubscription {

    private final JSONParseEngine mParser;

    public StreamMessage(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
        mParser = new JSONParseEngine(context);
    }

    @Override
    protected String getSubscriptionName() {
        return "stream-messages";
    }

    @Override
    protected String getSubscriptionCallbackName() {
        return "stream-messages";
    }

    @Override
    protected void onDocumentAdded(DDPSubscription.Added docEvent) throws JSONException {
        if(!docEvent.fields.isNull("args")) {
            final JSONArray args = docEvent.fields.getJSONArray("args");
            final String path = args.getString(0);
            final JSONObject message = args.getJSONObject(1);
            final String messageID = message.getString("_id");

            mParser.parseMessage(message);
        }
    }
}
