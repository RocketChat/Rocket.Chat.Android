package chat.rocket.android.api;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Task;
import chat.rocket.android.realm_helper.RealmHelper;

public class PushHelper extends MethodCallHelper {
  public PushHelper(Context context, String serverConfigId) {
    super(context, serverConfigId);
  }

  public PushHelper(RealmHelper realmHelper,
                    DDPClientWrapper ddpClient) {
    super(realmHelper, ddpClient);
  }

  public Task<Void> pushUpdate(@NonNull String pushId, @NonNull String token,
                               @Nullable String userId) {
    return call("raix:push-update", TIMEOUT_MS, () -> {
      JSONObject param = new PushUpdate(pushId, token, userId).toJson();
      return new JSONArray().put(param);
    }).onSuccessTask(task -> Task.forResult(null));
  }

  public Task<Void> pushSetUser(String pushId) {
    return call("raix:push-setuser", TIMEOUT_MS, () -> new JSONArray().put(pushId))
        .onSuccessTask(task -> Task.forResult(null));
  }

  private static class PushUpdate {

    private String pushId;
    private String gcmToken;
    private String userId;

    PushUpdate(@NonNull String pushId, @NonNull String gcmToken, @Nullable String userId) {
      this.pushId = pushId;
      this.gcmToken = gcmToken;
      this.userId = userId;
    }

    JSONObject toJson() throws JSONException {
      JSONObject param = new JSONObject();
      param.put("id", pushId);
      param.put("appName", "main");
      param.put("userId", userId != null ? userId : JSONObject.NULL);
      param.put("metadata", new JSONObject());

      JSONObject tokenParam = new JSONObject();
      tokenParam.put("gcm", gcmToken);

      param.put("token", tokenParam);

      return param;
    }
  }
}
