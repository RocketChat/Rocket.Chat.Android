package chat.rocket.android.service;

import java.util.List;
import chat.rocket.core.models.ServerInfo;

/**
 * interfaces used for RocketChatService and RocketChatwebSocketThread.
 */
/*package*/ interface ConnectivityManagerInternal {
  int REASON_CLOSED_BY_USER = 101;
  int REASON_NETWORK_ERROR = 102;
  int REASON_SERVER_ERROR = 103;
  int REASON_UNKNOWN = 104;

  void resetConnectivityStateList();

  void ensureConnections();

  List<ServerInfo> getServerList();

  ServerInfo getServerInfoForHost(String hostname);

  void notifyConnectionEstablished(String hostname, String session);

  void notifyConnectionLost(String hostname, int reason);
}
