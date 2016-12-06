package chat.rocket.android.service.ddp.stream;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.RealmObject;
import org.json.JSONArray;
import org.json.JSONException;

public class StreamNotifyUserSubscriptionsChanged extends AbstractStreamNotifyEventSubscriber {
  private final String userId;

  public StreamNotifyUserSubscriptionsChanged(Context context, RealmHelper realmHelper,
      DDPClientWraper ddpClient, String userId) {
    super(context, realmHelper, ddpClient);
    this.userId = userId;
  }

  @Override protected String getSubscriptionName() {
    return "stream-notify-user";
  }

  @Override protected JSONArray getSubscriptionParams() throws JSONException {
    return new JSONArray()
        .put(userId + "/subscriptions-changed")
        .put(false);
  }

  @Override protected Class<? extends RealmObject> getModelClass() {
    return RoomSubscription.class;
  }

  @Override protected String getPrimaryKeyForModel() {
    return "rid";
  }
}
