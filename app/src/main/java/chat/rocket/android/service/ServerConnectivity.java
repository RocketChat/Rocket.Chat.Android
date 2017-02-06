package chat.rocket.android.service;

/**
 * pair with server's hostname and its connectivity state.
 */
public class ServerConnectivity {
  public static final int STATE_CONNECTED = 1;
  public static final int STATE_DISCONNECTED = 2;
  /*package*/ static final int STATE_CONNECTING = 3;
  /*package*/ static final int STATE_DISCONNECTING = 4;

  public final String hostname;
  public final int state;

  public ServerConnectivity(String hostname, int state) {
    this.hostname = hostname;
    this.state = state;
  }

  /**
   * This exception should be thrown when connection is lost during waiting for CONNECTED.
   */
  public static class DisconnectedException extends Exception {
    public DisconnectedException() {
      super("Disconnected");
    }
  }
}
