package chat.rocket.android.notification;

import android.app.Notification;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import chat.rocket.android.R;
import org.json.JSONObject;

/**
 * utility class for notification.
 */
public class StreamNotifyUserNotifier implements Notifier {
  private final Context context;
  private final String title;
  private final String text;
  private final JSONObject payload;

  public StreamNotifyUserNotifier(Context context, String title, String text, JSONObject payload) {
    this.context = context;
    this.title = title;
    this.text = text;
    this.payload = payload;
  }

  @Override public void publishNotificationIfNeeded() {
    if (!shouldNotify()) {
      return;
    }

    NotificationManagerCompat.from(context)
        .notify(generateNotificationId(), generateNotification());
  }

  private boolean shouldNotify() {
    // TODO: should check if target message is already read or not.
    return true;
  }

  private int generateNotificationId() {
    // TODO: should summary notification by user or room.
    return (int) (System.currentTimeMillis() % Integer.MAX_VALUE);
  }

  private Notification generateNotification() {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
        .setContentTitle(title)
        .setContentText(text)
        .setAutoCancel(true)
        .setSmallIcon(R.drawable.rocket_chat_notification_24dp);
    if (text.length() > 20) {
      return new NotificationCompat.BigTextStyle(builder)
          .bigText(text)
          .build();
    } else {
      return builder.build();
    }
  }

}
