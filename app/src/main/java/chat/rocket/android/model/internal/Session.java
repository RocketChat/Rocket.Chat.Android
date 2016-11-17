package chat.rocket.android.model.internal;

import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.realm_helper.RealmHelper;
import hugo.weaving.DebugLog;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONObject;

/**
 * Login session info.
 */
public class Session extends RealmObject {
  public static final int DEFAULT_ID = 0;
  @PrimaryKey private int sessionId; //only 0 is used!
  private String token;
  private boolean tokenVerified;
  private String error;

  public int getSessionId() {
    return sessionId;
  }

  public void setSessionId(int sessionId) {
    this.sessionId = sessionId;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public boolean isTokenVerified() {
    return tokenVerified;
  }

  public void setTokenVerified(boolean tokenVerified) {
    this.tokenVerified = tokenVerified;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  /**
   * Log the server connection is lost due to soem exception.
   */
  @DebugLog public static void logError(RealmHelper realmHelper, Exception exception) {
    // TODO: should remove token if 403.
    realmHelper.executeTransaction(
        realm -> realm.createOrUpdateObjectFromJson(Session.class, new JSONObject()
            .put("sessionId", Session.DEFAULT_ID)
            .put("tokenVerified", false)
            .put("error", exception.getMessage())))
        .continueWith(new LogcatIfError());
  }
}
