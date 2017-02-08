package chat.rocket.android.model.internal;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;
import org.json.JSONObject;

import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.persistence.realm.RealmHelper;
import hugo.weaving.DebugLog;

/**
 * Login session info.
 */
public class Session extends RealmObject {

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

  public static RealmQuery<Session> queryDefaultSession(Realm realm) {
    return realm.where(Session.class).equalTo(ID, Session.DEFAULT_ID);
  }

  /**
   * Log the server connection is lost due to some exception.
   */
  @DebugLog
  public static void logError(RealmHelper realmHelper, Exception exception) {
    String errString = exception.getMessage();
    if (!TextUtils.isEmpty(errString) && errString.contains(AUTH_ERROR_CODE)) {
      realmHelper.executeTransaction(realm -> {
        realm.delete(Session.class);
        return null;
      }).continueWith(new LogcatIfError());
    } else {
      realmHelper.executeTransaction(
          realm -> realm.createOrUpdateObjectFromJson(Session.class, new JSONObject()
              .put(ID, Session.DEFAULT_ID)
              .put(TOKEN_VERIFIED, false)
              .put(ERROR, errString)))
          .continueWith(new LogcatIfError());
    }
  }

  /**
   * retry authentication.
   */
  @DebugLog
  public static void retryLogin(RealmHelper realmHelper) {
    final Session session = realmHelper.executeTransactionForRead(realm ->
        queryDefaultSession(realm).isNotNull(TOKEN).findFirst());

    if (!session.isTokenVerified() || !TextUtils.isEmpty(session.getError())) {
      realmHelper.executeTransaction(
          realm -> realm.createOrUpdateObjectFromJson(Session.class, new JSONObject()
              .put(ID, Session.DEFAULT_ID)
              .put(TOKEN_VERIFIED, false)
              .put(ERROR, JSONObject.NULL)))
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
}
