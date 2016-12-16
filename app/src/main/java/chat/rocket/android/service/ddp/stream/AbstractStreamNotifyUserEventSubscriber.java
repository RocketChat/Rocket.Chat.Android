package chat.rocket.android.service.ddp.stream;

import android.content.Context;

import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.realm_helper.RealmHelper;

abstract class AbstractStreamNotifyUserEventSubscriber extends AbstractStreamNotifyEventSubscriber {
  protected final String userId;

  protected AbstractStreamNotifyUserEventSubscriber(Context context, String hostname,
                                                    RealmHelper realmHelper,
                                                    DDPClientWrapper ddpClient, String userId) {
    super(context, hostname, realmHelper, ddpClient);
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
