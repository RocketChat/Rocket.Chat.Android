package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.os.Looper;

import org.json.JSONException;

import chat.rocket.android.api.JSONParseEngine;
import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android_ddp.DDPSubscription;

public class FilteredUsers extends AbstractRocketChatSubscription {

    private JSONParseEngine mParser;
    public FilteredUsers(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
        mParser = new JSONParseEngine(context);
    }

    @Override
    protected String getSubscriptionName() {
        return "filteredUsers";
    }

    @Override
    protected String getSubscriptionCallbackName() {
        return "filtered-users";
    }

    @Override
    protected void onDocumentAdded(DDPSubscription.Added docEvent) throws JSONException {
        final String userID = docEvent.docID;
        mParser.parseUser(userID, docEvent.fields);
    }

    @Override
    protected void onDocumentChanged(DDPSubscription.Changed docEvent) throws JSONException {
        final String userID = docEvent.docID;
        mParser.parseUser(userID, docEvent.fields);
    }
}
