package chat.rocket.android.service.ddp_subscription;

import android.content.Context;

import chat.rocket.android.model.doc.MeteorLoginServiceConfiguration;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import io.realm.RealmObject;

public class LoginServiceConfigurationSubscriber extends AbstractDDPDocEventSubscriber {
    public LoginServiceConfigurationSubscriber(Context context, RocketChatWebSocketAPI api) {
        super(context, api);
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
    protected Class<? extends RealmObject> getModelClass() {
        return MeteorLoginServiceConfiguration.class;
    }
}
