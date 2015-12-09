package chat.rocket.android.content.subscriber;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Looper;

import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.api.ws.RocketChatWSAPI;
import chat.rocket.android.content.RocketChatDatabaseHelper;
import chat.rocket.android.model.ServerConfig;
import jp.co.crowdworks.android_ddp.ddp.DDPSubscription;

public class LoginServiceConfiguration extends AbstractRocketChatSubscription {
    public LoginServiceConfiguration(Context context, Looper looper, RocketChatWSAPI api) {
        super(context, looper, api);
    }

    @Override
    protected String getSubscriptionName() {
        return "meteor.loginServiceConfiguration";
    }

    @Override
    protected String getSubscriptionCallbackName() {
        return "meteor_accounts_loginServiceConfiguration";
    }

    @Override
    protected void onDocumentAdded(DDPSubscription.Added docEvent) throws JSONException {
        ServerConfig conf = RocketChatDatabaseHelper.read(mContext, new RocketChatDatabaseHelper.DBCallback<ServerConfig>() {
            @Override
            public ServerConfig process(SQLiteDatabase db) throws Exception {
                return ServerConfig.getPrimaryConfig(db);
            }
        });

        if(conf==null) return;

        JSONObject providers = conf.getOAuthProviders();

        final String service = docEvent.fields.getString("service");

        if (!providers.isNull(service)) providers.remove(service);

        if("github".equals(service) || "google".equals(service) || "linkedin".equals(service)) {
            providers.put(service, new JSONObject().put("client_id", docEvent.fields.getString("clientId")));
        }
        else if("twitter".equals(service)) {
            providers.put(service, new JSONObject().put("consumer_key", docEvent.fields.getString("consumerKey")));
        }
        else if("facebook".equals(service)) {
            providers.put(service, new JSONObject().put("app_id", docEvent.fields.getString("appId")));
        }

        conf.oauthProviders = providers.toString();
        conf.putByContentProvider(mContext);
    }
}
