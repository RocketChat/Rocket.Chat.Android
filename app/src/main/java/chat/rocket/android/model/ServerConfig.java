package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Server configuration.
 */
@Deprecated
public class ServerConfig extends RealmObject {

  @SuppressWarnings({"PMD.ShortVariable"})
  public static final String ID = "serverConfigId";
  public static final String HOSTNAME = "hostname";
  public static final String STATE = "state";
  public static final String SESSION = "session";
  public static final String SECURE_CONNECTION = "secureConnection";
  public static final String ERROR = "error";

  public static final int STATE_READY = 0;
  public static final int STATE_CONNECTING = 1;
  public static final int STATE_CONNECTED = 2;
  public static final int STATE_CONNECTION_ERROR = 3;

  @PrimaryKey private String serverConfigId;
  private String hostname;
  private int state;
  private String session;
  private boolean secureConnection;
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

  public boolean usesSecureConnection() {
    return secureConnection;
  }

  public void setSecureConnection(boolean usesSecureConnection) {
    this.secureConnection = usesSecureConnection;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }
}
