package chat.rocket.android.service.ddp.stream;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.RealmObject;

public class StreamNotifyUserSubscriptionsChanged extends AbstractStreamNotifyUserEventSubscriber {
  public StreamNotifyUserSubscriptionsChanged(Context context, RealmHelper realmHelper,
      DDPClientWraper ddpClient, String userId) {
    super(context, realmHelper, ddpClient, userId);
  }

  @Override protected String getSubscriptionSubParam() {
    return "subscriptions-changed";
  }

  @Override protected Class<? extends RealmObject> getModelClass() {
    return RoomSubscription.class;
  }

  @Override protected String getPrimaryKeyForModel() {
    return "rid";
  }
}
