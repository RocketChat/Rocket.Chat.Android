package chat.rocket.android.service.ddp.stream;

import android.content.Context;
import io.realm.RealmObject;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.realm_helper.RealmHelper;

public class StreamNotifyUserSubscriptionsChanged extends AbstractStreamNotifyUserEventSubscriber {
  public StreamNotifyUserSubscriptionsChanged(Context context, String hostname,
                                              RealmHelper realmHelper, DDPClientWrapper ddpClient,
                                              String userId) {
    super(context, hostname, realmHelper, ddpClient, userId);
  }

  @Override
  protected String getSubscriptionSubParam() {
    return "subscriptions-changed";
  }

  @Override
  protected Class<? extends RealmObject> getModelClass() {
    return RoomSubscription.class;
  }

  @Override
  protected JSONObject customizeFieldJson(JSONObject json) throws JSONException {
    return RoomSubscription.customizeJson(super.customizeFieldJson(json));
  }

  @Override
  protected String getPrimaryKeyForModel() {
    return "rid";
  }
}
