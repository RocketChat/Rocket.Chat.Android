package chat.rocket.android.service.observer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.activity.MainActivity;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.helper.Avatar;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.internal.NotificationItem;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;
import chat.rocket.android.service.notification.NotificationDismissalCallbackService;

/**
 * observes NotificationItem and notify/cancel notification.
 */
public class NotificationItemObserver extends AbstractModelObserver<NotificationItem> {
  public NotificationItemObserver(Context context, String hostname, RealmHelper realmHelper,
                                  DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override
  public RealmResults<NotificationItem> queryItems(Realm realm) {
    return realm.where(NotificationItem.class).findAll();
  }

  @Override
  public void onUpdateResults(List<NotificationItem> results) {
    if (results.isEmpty()) {
      return;
    }

    for (NotificationItem item : results) {
      final String notificationId = item.getRoomId();

      if (item.getUnreadCount() > 0
          && item.getContentUpdatedAt() > item.getLastSeenAt()) {
        generateNotificationFor(item)
            .onSuccess(task -> {
              Notification notification = task.getResult();
              if (notification != null) {
                NotificationManagerCompat.from(context)
                    .notify(notificationId.hashCode(), notification);
              }
              return null;
            });
      } else {
        NotificationManagerCompat.from(context).cancel(notificationId.hashCode());
      }
    }
  }

  private Task<Notification> generateNotificationFor(NotificationItem item) {
    final String username = item.getSenderName();
    final String roomId = item.getRoomId();
    final String title = item.getTitle();
    final String description = TextUtils.or(item.getDescription(), "").toString();
    final int unreadCount = item.getUnreadCount();

    if (TextUtils.isEmpty(username)) {
      return Task.forResult(generateNotification(roomId, title, description, unreadCount, null));
    }

    int size = context.getResources().getDimensionPixelSize(R.dimen.notification_avatar_size);
    return new Avatar(hostname, username).getBitmap(context, size)
        .continueWithTask(task -> {
          Bitmap icon = task.isFaulted() ? null : task.getResult();
          final Notification notification =
              generateNotification(roomId, title, description, unreadCount, icon);
          return Task.forResult(notification);
        });
  }

  private PendingIntent getContentIntent(String roomId) {
    Intent intent = new Intent(context, MainActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    ServerConfig config = RealmStore.getDefault().executeTransactionForRead(realm ->
        realm.where(ServerConfig.class).equalTo("hostname", hostname).findFirst());
    if (config != null) {
      intent.putExtra("serverConfigId", config.getServerConfigId());
      intent.putExtra("roomId", roomId);
    }

    return PendingIntent.getActivity(context.getApplicationContext(),
        (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
        intent, PendingIntent.FLAG_ONE_SHOT);
  }

  private PendingIntent getDeleteIntent(String roomId) {
    Intent intent = new Intent(context, NotificationDismissalCallbackService.class);
    ServerConfig config = RealmStore.getDefault().executeTransactionForRead(realm ->
        realm.where(ServerConfig.class).equalTo("hostname", hostname).findFirst());
    if (config != null) {
      intent.putExtra("serverConfigId", config.getServerConfigId());
      intent.putExtra("roomId", roomId);
    }

    return PendingIntent.getService(context.getApplicationContext(),
        (int) (System.currentTimeMillis() % Integer.MAX_VALUE),
        intent, PendingIntent.FLAG_ONE_SHOT);
  }

  private Notification generateNotification(String roomId, String title,
                                            @NonNull String description, int unreadCount,
                                            @Nullable Bitmap icon) {

    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        .setContentTitle(title)
        .setContentText(description)
        .setNumber(unreadCount)
        .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
        .setSmallIcon(R.drawable.rocket_chat_notification)
        .setContentIntent(getContentIntent(roomId))
        .setDeleteIntent(getDeleteIntent(roomId));

    if (icon != null) {
      builder.setLargeIcon(icon);
    }

    if (description.length() > 20) {
      return new NotificationCompat.BigTextStyle(builder)
          .bigText(description)
          .build();
    } else {
      return builder.build();
    }
  }
}
