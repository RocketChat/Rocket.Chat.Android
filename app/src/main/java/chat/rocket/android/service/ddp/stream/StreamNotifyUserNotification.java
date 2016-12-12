package chat.rocket.android.service.ddp.stream;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.notification.Notifier;
import chat.rocket.android.notification.StreamNotifyUserNotifier;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.RealmObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StreamNotifyUserNotification extends AbstractStreamNotifyUserEventSubscriber {
  public StreamNotifyUserNotification(Context context, String hostname, RealmHelper realmHelper,
      DDPClientWraper ddpClient, String userId) {
    super(context, hostname, realmHelper, ddpClient, userId);
  }

  @Override protected String getSubscriptionSubParam() {
    return "notification";
  }

  @Override protected void handleArgs(JSONArray args) throws JSONException {
    JSONObject target = args.getJSONObject(args.length() - 1);
    Notifier notifier = new StreamNotifyUserNotifier(context, hostname,
        target.getString("title"),
        target.getString("text"),
        target.getJSONObject("payload"));
    notifier.publishNotificationIfNeeded();
  }

  @Override protected Class<? extends RealmObject> getModelClass() {
    // not used because handleArgs is override.
    return null;
  }

  @Override protected String getPrimaryKeyForModel() {
    // not used because handleArgs is override.
    return null;
  }
}
