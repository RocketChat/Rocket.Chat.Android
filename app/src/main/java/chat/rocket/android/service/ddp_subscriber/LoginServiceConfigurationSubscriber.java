package chat.rocket.android.service.ddp_subscriber;

import android.content.Context;
import chat.rocket.android.model.MeteorLoginServiceConfiguration;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import io.realm.RealmObject;

/**
 * meteor.loginServiceConfiguration subscriber
 */
public class LoginServiceConfigurationSubscriber extends AbstractDDPDocEventSubscriber {
  public LoginServiceConfigurationSubscriber(Context context, String serverConfigId,
      RocketChatWebSocketAPI api) {
    super(context, serverConfigId, api);
  }

  @Override protected String getSubscriptionName() {
    return "meteor.loginServiceConfiguration";
  }

  @Override protected String getSubscriptionCallbackName() {
    return "meteor_accounts_loginServiceConfiguration";
  }

  @Override protected Class<? extends RealmObject> getModelClass() {
    return MeteorLoginServiceConfiguration.class;
  }
}
