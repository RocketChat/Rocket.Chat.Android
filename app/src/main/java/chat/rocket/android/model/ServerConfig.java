package chat.rocket.android.model;

import bolts.Task;
import chat.rocket.android.helper.LogcatIfError;
import hugo.weaving.DebugLog;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.annotations.PrimaryKey;
import jp.co.crowdworks.realm_java_helpers.RealmHelper;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;

/**
 * Server configuration.
 */
public class ServerConfig extends RealmObject {
  @PrimaryKey private String serverConfigId;
  private String hostname;
  private String connectionError;
  private String session;
  private String token;
  private boolean tokenVerified;

  public static RealmQuery<ServerConfig> queryLoginRequiredConnections(Realm realm) {
    return realm.where(ServerConfig.class).equalTo("tokenVerified", false);
  }

  /**
   * Check if connection login required exists.
   */
  public static boolean hasLoginRequiredConnection() {
    ServerConfig config =
        RealmHelper.executeTransactionForRead(realm ->
            queryLoginRequiredConnections(realm).findFirst());

    return config != null;
  }

  /**
   * Request token refresh.
   */
  public static Task<Void> forceInvalidateToken() {
    return RealmHelperBolts.executeTransaction(realm -> {
      RealmResults<ServerConfig> targetConfigs = realm.where(ServerConfig.class)
          .isNotNull("token")
          .equalTo("tokenVerified", true)
          .findAll();
      for (ServerConfig config : targetConfigs) {
        config.setTokenVerified(false);
      }
      return null;
    });
  }

  /**
   * Log the server connection is lost due to soem exception.
   */
  @DebugLog public static void logConnectionError(String serverConfigId, Exception exception) {
    RealmHelperBolts.executeTransaction(
        realm -> realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
            .put("serverConfigId", serverConfigId)
            .put("connectionError", exception.getMessage())
            .put("session", JSONObject.NULL)))
        .continueWith(new LogcatIfError());
  }

  public String getServerConfigId() {
    return serverConfigId;
  }

  public void setServerConfigId(String serverConfigId) {
    this.serverConfigId = serverConfigId;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public String getConnectionError() {
    return connectionError;
  }

  public void setConnectionError(String connectionError) {
    this.connectionError = connectionError;
  }

  public String getSession() {
    return session;
  }

  public void setSession(String session) {
    this.session = session;
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
}
