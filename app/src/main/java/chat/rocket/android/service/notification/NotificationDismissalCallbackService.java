package chat.rocket.android.service.notification;

import android.app.IntentService;
import android.content.Intent;
import chat.rocket.android.model.internal.NotificationItem;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;

/**
 * triggered when notification is dismissed.
 */
public class NotificationDismissalCallbackService extends IntentService {
  public NotificationDismissalCallbackService() {
    super(NotificationDismissalCallbackService.class.getSimpleName());
  }

  @Override protected void onHandleIntent(Intent intent) {
    if (!intent.hasExtra("serverConfigId") || !intent.hasExtra("roomId")) {
      return;
    }

    String serverConfigId = intent.getStringExtra("serverConfigId");
    String roomId = intent.getStringExtra("roomId");

    RealmHelper realmHelper = RealmStore.get(serverConfigId);
    if (realmHelper == null) {
      return;
    }

    realmHelper.executeTransaction(realm -> {
      NotificationItem item =
          realm.where(NotificationItem.class).equalTo("roomId", roomId).findFirst();
      if (item != null) {
        long currentTime = System.currentTimeMillis();
        if (item.getLastSeenAt() <= currentTime) {
          item.setLastSeenAt(currentTime);
        }
      }
      return null;
    });
  }
}
