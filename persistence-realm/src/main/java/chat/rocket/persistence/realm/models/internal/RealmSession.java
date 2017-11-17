package chat.rocket.persistence.realm.models.internal;

import android.text.TextUtils;

import org.json.JSONObject;

import chat.rocket.core.models.Session;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.helpers.LogcatIfError;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;

/**
 * Login session info.
 */
public class RealmSession extends RealmObject {

  @SuppressWarnings({"PMD.ShortVariable"})
  public static final String ID = "sessionId";
  public static final String TOKEN = "token";
  public static final String TOKEN_VERIFIED = "tokenVerified";
  public static final String ERROR = "error";

  public static final int DEFAULT_ID = 0;
  public static final String AUTH_ERROR_CODE = "[403]";

  @PrimaryKey private int sessionId; //only 0 is used!
  private String token;
  private boolean tokenVerified;
  private String error;

  public static RealmQuery<RealmSession> queryDefaultSession(Realm realm) {
    return realm.where(RealmSession.class).equalTo(ID, RealmSession.DEFAULT_ID);
  }

  /**
   * Log the server connection is lost due to some exception.
   */
  @DebugLog
  public static void logError(RealmHelper realmHelper, Exception exception) {
    String errString = exception.getMessage();
    if (!TextUtils.isEmpty(errString) && errString.contains(AUTH_ERROR_CODE)) {
      realmHelper.executeTransaction(realm -> {
        realm.delete(RealmSession.class);
        return null;
      }).continueWith(new LogcatIfError());
    } else {
      realmHelper.executeTransaction(
          realm -> realm.createOrUpdateObjectFromJson(RealmSession.class, new JSONObject()
              .put(ID, RealmSession.DEFAULT_ID)
              .put(TOKEN_VERIFIED, false)
              .put(ERROR, errString)))
          .continueWith(new LogcatIfError());
    }
  }

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

  public Session asSession() {
    return Session.builder()
        .setSessionId(sessionId)
        .setToken(token)
        .setTokenVerified(tokenVerified)
        .setError(error)
        .build();
  }
}
