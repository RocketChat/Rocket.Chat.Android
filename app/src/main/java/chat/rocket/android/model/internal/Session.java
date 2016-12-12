package chat.rocket.android.model.internal;

import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.realm_helper.RealmHelper;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
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

  public static RealmQuery<Session> queryDefaultSession(Realm realm) {
    return realm.where(Session.class).equalTo("sessionId", Session.DEFAULT_ID);
  }

  /**
   * Log the server connection is lost due to soem exception.
   */
  @DebugLog public static void logError(RealmHelper realmHelper, Exception exception) {
    String errString = exception.getMessage();
    if (!TextUtils.isEmpty(errString) && errString.contains("[403]")) {
      realmHelper.executeTransaction(realm -> {
        realm.delete(Session.class);
        return null;
      }).continueWith(new LogcatIfError());
    } else {
      realmHelper.executeTransaction(
          realm -> realm.createOrUpdateObjectFromJson(Session.class, new JSONObject()
              .put("sessionId", Session.DEFAULT_ID)
              .put("tokenVerified", false)
              .put("error", errString)))
          .continueWith(new LogcatIfError());
    }
  }
}
