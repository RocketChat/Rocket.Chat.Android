package chat.rocket.android.service.ddp.base;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.RealmObject;

/**
 * "userData" subscriber.
 */
public class UserDataSubscriber extends AbstractBaseSubscriber {
  public UserDataSubscriber(Context context, String hostname, RealmHelper realmHelper,
      DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override protected String getSubscriptionName() {
    return "userData";
  }

  @Override protected String getSubscriptionCallbackName() {
    return "users";
  }

  @Override protected Class<? extends RealmObject> getModelClass() {
    return User.class;
  }
}
