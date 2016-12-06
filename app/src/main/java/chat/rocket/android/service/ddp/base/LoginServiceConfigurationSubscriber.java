package chat.rocket.android.service.ddp.base;

import android.content.Context;
import chat.rocket.android.model.ddp.MeteorLoginServiceConfiguration;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.api.DDPClientWraper;
import io.realm.RealmObject;

/**
 * meteor.loginServiceConfiguration subscriber
 */
public class LoginServiceConfigurationSubscriber extends AbstractBaseSubscriber {
  public LoginServiceConfigurationSubscriber(Context context, RealmHelper realmHelper,
      DDPClientWraper ddpClient) {
    super(context, realmHelper, ddpClient);
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
