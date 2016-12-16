package chat.rocket.android.api;

import android.support.annotation.Nullable;

import bolts.Task;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.DDPSubscription;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;

import rx.Observable;

/**
 * DDP client wrapper.
 */
public class DDPClientWrapper {
  private final DDPClient ddpClient;
  private final String hostname;

  private DDPClientWrapper(String hostname) {
    ddpClient = new DDPClient(OkHttpHelper.getClientForWebSocket());
    this.hostname = hostname;
  }

  /**
   * create new API client instance.
   */
  public static DDPClientWrapper create(String hostname) {
    return new DDPClientWrapper(hostname);
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
    final String subscriptionId = UUID.randomUUID().toString();
    RCLog.d("sub:[%s]> %s(%s)", subscriptionId, name, param);
    return ddpClient.sub(subscriptionId, name, param);
  }

  /**
   * Unsubscribe with DDP client.
   */
  public Task<DDPSubscription.NoSub> unsubscribe(final String subscriptionId) {
    RCLog.d("unsub:[%s]>", subscriptionId);
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
   * Execute raw RPC.
   */
  public Task<DDPClientCallback.RPC> rpc(String methodCallId, String methodName, String params,
                                         long timeoutMs) {
    RCLog.d("rpc:[%s]> %s(%s) timeout=%d", methodCallId, methodName, params, timeoutMs);
    if (TextUtils.isEmpty(params)) {
      return ddpClient.rpc(methodName, null, methodCallId, timeoutMs).continueWithTask(task -> {
        if (task.isFaulted()) {
          RCLog.d("rpc:[%s]< error = %s", methodCallId, task.getError());
        } else {
          RCLog.d("rpc:[%s]< result = %s", methodCallId, task.getResult().result);
        }
        return task;
      });
    }

    try {
      return ddpClient.rpc(methodName, new JSONArray(params), methodCallId, timeoutMs)
          .continueWithTask(task -> {
            if (task.isFaulted()) {
              RCLog.d("rpc:[%s]< error = %s", methodCallId, task.getError());
            } else {
              RCLog.d("rpc:[%s]< result = %s", methodCallId, task.getResult().result);
            }
            return task;
          });
    } catch (JSONException exception) {
      return Task.forError(exception);
    }
  }
}
