package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import chat.rocket.android.api.JSONParseEngine;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;

public class RocketChatSubscription extends AbstractRocketChatSubscription {

    private final JSONParseEngine mParser;
    private final HashMap<String, String> mDocumentStore;

    public RocketChatSubscription(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
        mParser = new JSONParseEngine(context);
        mDocumentStore = new HashMap<>();
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
    protected void onDocumentAdded(DDPSubscription.Added docEvent) throws JSONException {
        JSONObject room = docEvent.fields;
        mParser.parseRoom(room);
        mDocumentStore.put(docEvent.docID, room.getString("rid"));
    }

    @Override
    protected void onDocumentChanged(DDPSubscription.Changed docEvent) throws JSONException {
        JSONObject room = docEvent.fields;
        if(mDocumentStore.containsKey(docEvent.docID)){
            room.put("rid", mDocumentStore.get(docEvent.docID));
            mParser.parseRoom(room);
        }
    }
}
