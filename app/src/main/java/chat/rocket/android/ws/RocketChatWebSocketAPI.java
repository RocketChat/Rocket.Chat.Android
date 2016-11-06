package chat.rocket.android.ws;

import android.support.annotation.Nullable;
import bolts.Task;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.DDPSubscription;
import java.util.UUID;
import org.json.JSONArray;
import rx.Observable;

/**
 * API for several POST actions.
 */
public class RocketChatWebSocketAPI {
  private final DDPClient ddpClient;
  private final String hostname;

  private RocketChatWebSocketAPI(String hostname) {
    ddpClient = new DDPClient(OkHttpHelper.getClientForWebSocket());
    this.hostname = hostname;
  }

  /**
   * create new API client instance.
   */
  public static RocketChatWebSocketAPI create(String hostname) {
    return new RocketChatWebSocketAPI(hostname);
  }

  /**
   * Connect to WebSocket server with DDP client.
   */
  public Task<DDPClientCallback.Connect> connect(@Nullable String session) {
    return ddpClient.connect("wss://" + hostname + "/websocket", session);
  }

  /**
   * Returns whether DDP client is connected to WebSocket server.
   */
  public boolean isConnected() {
    return ddpClient.isConnected();
  }

  /**
   * close connection.
   */
  public void close() {
    ddpClient.close();
  }

  /**
   * Subscribe with DDP client.
   */
  public Task<DDPSubscription.Ready> subscribe(final String name, JSONArray param) {
    return ddpClient.sub(UUID.randomUUID().toString(), name, param);
  }

  /**
   * Unsubscribe with DDP client.
   */
  public Task<DDPSubscription.NoSub> unsubscribe(final String subscriptionId) {
    return ddpClient.unsub(subscriptionId);
  }

  /**
   * Returns Observable for handling DDP subscription.
   */
  public Observable<DDPSubscription.Event> getSubscriptionCallback() {
    return ddpClient.getSubscriptionCallback();
  }
}
