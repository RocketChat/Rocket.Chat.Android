package chat.rocket.android.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import bolts.Task;
import chat.rocket.persistence.realm.RealmHelper;

public class RaixPushHelper extends MethodCallHelper {
  public RaixPushHelper(Context context, String hostname) {
    super(context, hostname);
  }

  public RaixPushHelper(RealmHelper realmHelper) {
    super(realmHelper);
  }

  public Task<Void> pushUpdate(@NonNull String pushId, @NonNull String gcmToken,
                               @Nullable String userId) {
    return call("raix:push-update", TIMEOUT_MS, () ->
        new JSONArray().put(new JSONObject()
            .put("id", pushId)
            .put("appName", "main")
            .put("userId", userId != null ? userId : JSONObject.NULL)
            .put("metadata", new JSONObject())
            .put("token", new JSONObject().put("gcm", gcmToken))))
        .onSuccessTask(task -> Task.forResult(null));
  }

  public Task<Void> pushSetUser(String pushId) {
    return call("raix:push-setuser", TIMEOUT_MS, () -> new JSONArray().put(pushId))
        .onSuccessTask(task -> Task.forResult(null));
  }
}
