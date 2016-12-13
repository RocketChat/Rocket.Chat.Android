package chat.rocket.android.service.observer;

import android.content.Context;
import android.support.v4.app.NotificationManagerCompat;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.realm_helper.RealmHelper;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;

/**
 * observing room subscriptions with unread>0.
 */
public class ReactiveNotificationCancelManager extends AbstractModelObserver<RoomSubscription> {
  public ReactiveNotificationCancelManager(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override public RealmResults<RoomSubscription> queryItems(Realm realm) {
    return realm.where(RoomSubscription.class)
        .equalTo("open", true)
        .equalTo("unread", 0)
        .findAll();
  }

  @DebugLog
  @Override public void onUpdateResults(List<RoomSubscription> roomSubscriptions) {
    // TODO implement!

    for (RoomSubscription roomSubscription : roomSubscriptions) {
      final String roomId = roomSubscription.getRid();
      NotificationManagerCompat.from(context).cancel(roomId.hashCode());
    }
  }
}
