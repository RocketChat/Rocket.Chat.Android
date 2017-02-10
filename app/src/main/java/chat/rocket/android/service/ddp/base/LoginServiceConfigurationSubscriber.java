package chat.rocket.android.service.ddp.base;

import android.content.Context;
import io.realm.RealmObject;

import chat.rocket.persistence.realm.models.ddp.RealmMeteorLoginServiceConfiguration;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.android.service.DDPClientRef;

/**
 * meteor.loginServiceConfiguration subscriber
 */
public class LoginServiceConfigurationSubscriber extends AbstractBaseSubscriber {
  public LoginServiceConfigurationSubscriber(Context context, String hostname,
                                             RealmHelper realmHelper, DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
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
    return RealmMeteorLoginServiceConfiguration.class;
  }
}
