package chat.rocket.android.service;

import java.util.List;

import chat.rocket.core.models.ServerInfo;

/**
 * interfaces used for RocketChatService and RocketChatwebSocketThread.
 */
/*package*/ interface ConnectivityManagerInternal {

  void resetConnectivityStateList();

  void ensureConnections();

  List<ServerInfo> getServerList();

  ServerInfo getServerInfoForHost(String hostname);

  void notifyConnectionEstablished(String hostname, String session);

  void notifyConnectionLost(String hostname, int reason);

  void notifyConnecting(String hostname);
}
