package chat.rocket.android.model;

import bolts.Task;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.realm_helper.RealmStore;
import hugo.weaving.DebugLog;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONObject;

/**
 * Server configuration.
 */
public class ServerConfig extends RealmObject {
  public static final int STATE_READY = 0;
  public static final int STATE_CONNECTING = 1;
  public static final int STATE_CONNECTED = 2;
  public static final int STATE_CONNECTION_ERROR = 3;

  @PrimaryKey private String serverConfigId;
  private String hostname;
  private int state;
  private String session;
  private String error;

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

  public int getState() {
    return state;
  }

  public void setState(int state) {
    this.state = state;
  }

  public String getSession() {
    return session;
  }

  public void setSession(String session) {
    this.session = session;
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
  @DebugLog public static void logConnectionError(String serverConfigId, Exception exception) {
    RealmStore.getDefault().executeTransaction(
        realm -> realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
            .put("serverConfigId", serverConfigId)
            .put("state", STATE_CONNECTION_ERROR)
            .put("error", exception.getMessage())))
        .continueWith(new LogcatIfError());
  }

  /**
   * Update the state of the ServerConfig with serverConfigId.
   */
  public static Task<Void> updateState(final String serverConfigId, int state) {
    return RealmStore.getDefault().executeTransaction(realm -> {
      ServerConfig config =
          realm.where(ServerConfig.class).equalTo("serverConfigId", serverConfigId).findFirst();
      if (config == null || config.getState() != state) {
        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
            .put("serverConfigId", serverConfigId)
            .put("state", state));
      }
      return null;
    });
  }
}
