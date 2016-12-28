package chat.rocket.android.model.params;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

public class PushUpdate {

  private String pushId;
  private String gcmToken;
  private String userId;

  public PushUpdate(@NonNull String pushId, @NonNull String gcmToken, @Nullable String userId) {
    this.pushId = pushId;
    this.gcmToken = gcmToken;
    this.userId = userId;
  }

  public JSONObject toJson() throws JSONException {
    JSONObject param = new JSONObject();
    param.put("id", pushId);
    param.put("appName", "main");
    param.put("userId", userId);
    param.put("metadata", new JSONObject());

    JSONObject tokenParam = new JSONObject();
    tokenParam.put("gcm", gcmToken);

    param.put("token", tokenParam);

    return param;
  }
}
