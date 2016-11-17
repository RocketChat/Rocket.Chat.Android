package chat.rocket.android.service.ddp;

import android.content.Context;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import io.realm.RealmObject;

/**
 * "activeUsers" subscriber.
 */
public class ActiveUsersSubscriber extends AbstractDDPDocEventSubscriber {
  public ActiveUsersSubscriber(Context context, RealmHelper realmHelper,
      RocketChatWebSocketAPI api) {
    super(context, realmHelper, api);
  }

  @Override protected String getSubscriptionName() {
    return "activeUsers";
  }

  @Override protected String getSubscriptionCallbackName() {
    return "users";
  }

  @Override protected Class<? extends RealmObject> getModelClass() {
    return User.class;
  }
}
