package chat.rocket.android.service.ddp;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.RealmObject;

/**
 * "userData" subscriber.
 */
public class UserDataSubscriber extends AbstractDDPDocEventSubscriber {
  public UserDataSubscriber(Context context, RealmHelper realmHelper,
      DDPClientWraper ddpClient) {
    super(context, realmHelper, ddpClient);
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
