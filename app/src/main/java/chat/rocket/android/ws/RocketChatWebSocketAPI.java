package chat.rocket.android.ws;

import android.support.annotation.Nullable;
import android.util.Patterns;
import bolts.Task;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.model.ServerConfigCredential;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.DDPSubscription;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

  private String generateId(String method) {
    return method + "-" + UUID.randomUUID().toString().replace("-", "");
  }

  /**
   * Login with ServerConfigCredential.
   */
  public Task<DDPClientCallback.RPC> login(ServerConfigCredential credential) {
    JSONObject param = new JSONObject();

    try {
      String authType = credential.getType();
      if ("email".equals(authType)) {
        String username = credential.getUsername();
        if (Patterns.EMAIL_ADDRESS.matcher(username).matches()) {
          param.put("user", new JSONObject().put("email", username));
        } else {
          param.put("user", new JSONObject().put("username", username));
        }
        param.put("password", new JSONObject()
            .put("digest", credential.getHashedPasswd())
            .put("algorithm", "sha-256"));
      } else if ("github".equals(authType) || "twitter".equals(authType)) {
        param.put("oauth", new JSONObject()
            .put("credentialToken", credential.getCredentialToken())
            .put("credentialSecret", credential.getCredentialSecret()));
      }
    }
    catch (JSONException e) {
      return Task.forError(e);
    }

    return ddpClient.rpc("login", new JSONArray().put(param), generateId("login"));
  }

  public Task<DDPClientCallback.RPC> loginWithToken(final String token) {
    JSONObject param = new JSONObject();
    try {
      param.put("resume", token);
    } catch (JSONException e) {
      return Task.forError(e);
    }

    return ddpClient.rpc("login", new JSONArray().put(param), generateId("login-token"));
  }

  public Task<DDPClientCallback.RPC> logout() {
    return ddpClient.rpc("logout", null, generateId("logout"));
  }

}
