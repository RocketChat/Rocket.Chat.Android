package chat.rocket.android.service.ddp.base;

import android.content.Context;
import io.realm.RealmObject;

import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmHelper;

/**
 * "activeUsers" subscriber.
 */
public class ActiveUsersSubscriber extends AbstractBaseSubscriber {
  public ActiveUsersSubscriber(Context context, String hostname, RealmHelper realmHelper,
                               DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override
  protected String getSubscriptionName() {
    return "activeUsers";
  }

  @Override
  protected String getSubscriptionCallbackName() {
    return "users";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return User.class;
  }
}
