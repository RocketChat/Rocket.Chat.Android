package chat.rocket.android.service.ddp.base;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.ddp.AbstractDDPDocEventSubscriber;
import org.json.JSONArray;

abstract class AbstractBaseSubscriber extends AbstractDDPDocEventSubscriber {
  protected AbstractBaseSubscriber(Context context, String hostname, RealmHelper realmHelper,
      DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override protected final JSONArray getSubscriptionParams() {
    return null;
  }

  @Override protected final boolean shouldTruncateTableOnInitialize() {
    return true;
  }

  protected abstract String getSubscriptionCallbackName();

  @Override protected final boolean isTarget(String callbackName) {
    return getSubscriptionCallbackName().equals(callbackName);
  }
}
