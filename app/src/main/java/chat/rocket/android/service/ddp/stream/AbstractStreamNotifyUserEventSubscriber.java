package chat.rocket.android.service.ddp.stream;

import android.content.Context;

import chat.rocket.persistence.realm.RealmHelper;

abstract class AbstractStreamNotifyUserEventSubscriber extends AbstractStreamNotifyEventSubscriber {
  protected final String userId;

  protected AbstractStreamNotifyUserEventSubscriber(Context context, String hostname,
                                                    RealmHelper realmHelper, String userId) {
    super(context, hostname, realmHelper);
    this.userId = userId;
  }

  @Override
  protected final String getSubscriptionName() {
    return "stream-notify-user";
  }

  @Override
  protected final String getSubscriptionParam() {
    return userId + "/" + getSubscriptionSubParam();
  }

  protected abstract String getSubscriptionSubParam();
}
