package chat.rocket.android.service.observer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import chat.rocket.android.R;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;

/**
 * observing room subscriptions with unread>0.
 */
public class ReactiveNotificationManager extends AbstractModelObserver<RoomSubscription> {
  public ReactiveNotificationManager(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override public RealmResults<RoomSubscription> queryItems(Realm realm) {
    return realm.where(RoomSubscription.class)
        .equalTo("open", true)
        .greaterThan("unread", 0)
        .findAll();
  }

  @DebugLog
  @Override public void onUpdateResults(List<RoomSubscription> roomSubscriptions) {
    // TODO implement!

    for (RoomSubscription roomSubscription : roomSubscriptions) {
      final String roomId = roomSubscription.getRid();

      Intent intent = new Intent(context, MainActivity.class);
      intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
      ServerConfig config = RealmStore.getDefault().executeTransactionForRead(realm ->
          realm.where(ServerConfig.class).equalTo("hostname", hostname).findFirst());
      if (config != null) {
        intent.putExtra("serverConfigId", config.getServerConfigId());
        intent.putExtra("roomId", roomId);
      }

      PendingIntent pendingIntent = PendingIntent.getActivity(context.getApplicationContext(),
          (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
          intent, PendingIntent.FLAG_ONE_SHOT);

      NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
          .setContentTitle(roomSubscription.getName())
          .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
          .setSmallIcon(R.drawable.rocket_chat_notification_24dp)
          .setContentIntent(pendingIntent);

      Notification notification =  builder.build();
      NotificationManagerCompat.from(context).notify(roomId.hashCode(), notification);
    }
  }
}
