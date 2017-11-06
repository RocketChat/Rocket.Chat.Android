package chat.rocket.android.service.ddp.base;

import android.content.Context;

import org.json.JSONArray;

import chat.rocket.android.service.ddp.AbstractDDPDocEventSubscriber;
import chat.rocket.persistence.realm.RealmHelper;

abstract class AbstractBaseSubscriber extends AbstractDDPDocEventSubscriber {
  protected AbstractBaseSubscriber(Context context, String hostname, RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
  }

  @Override
  protected final JSONArray getSubscriptionParams() {
    return null;
  }

  @Override
  protected final boolean shouldTruncateTableOnInitialize() {
    return false;
  }

  protected abstract String getSubscriptionCallbackName();

  @Override
  protected final boolean isTarget(String callbackName) {
    return getSubscriptionCallbackName().equals(callbackName);
  }
}
