package chat.rocket.android.service;


/**
 * Stores information just for required for initializing connectivity manager.
 */
public class ServerInfo {
  public final String hostname;
  public final String name;
  /*package*/ final String session;
  public final boolean insecure;

  public ServerInfo(String hostname, String name, String session, boolean insecure) {
    this.hostname = hostname;
    this.name = name;
    this.session = session;
    this.insecure = insecure;
  }
}
