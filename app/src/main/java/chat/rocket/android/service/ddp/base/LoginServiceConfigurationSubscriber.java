package chat.rocket.android.service.ddp.base;

import android.content.Context;
import io.realm.RealmObject;

import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import chat.rocket.android.realm_helper.RealmHelper;

/**
 * meteor.loginServiceConfiguration subscriber
 */
public class LoginServiceConfigurationSubscriber extends AbstractBaseSubscriber {
  public LoginServiceConfigurationSubscriber(Context context, String hostname,
                                             RealmHelper realmHelper, DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
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
