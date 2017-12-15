package chat.rocket.android.service;

/**
 * pair with server's hostname and its connectivity state.
 */
public class ServerConnectivity {

    public static final int STATE_DISCONNECTED = 0;
    /* package */ static final int STATE_DISCONNECTING = 1;
    /* package */ static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_SESSION_ESTABLISHED = 4;
    /* package */ static final ServerConnectivity CONNECTED = new ServerConnectivity(null, STATE_CONNECTED);

    public final String hostname;
    public final int state;
    public final int code;

    ServerConnectivity(String hostname, int state) {
        this.hostname = hostname;
        this.state = state;
        this.code = -1;
    }

    ServerConnectivity(String hostname, int state, int code) {
        this.hostname = hostname;
        this.state = state;
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerConnectivity that = (ServerConnectivity) o;

        return state == that.state;
    }

    @Override
    public int hashCode() {
        return state;
    }

    /**
     * This exception should be thrown when connection is lost during waiting for CONNECTED.
     */
  /* package */static class DisconnectedException extends Exception {
        /* package */DisconnectedException() {
            super("Disconnected");
        }
    }
}
