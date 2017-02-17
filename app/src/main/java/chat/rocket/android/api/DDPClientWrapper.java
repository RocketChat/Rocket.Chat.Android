package chat.rocket.android.api;

import android.support.annotation.Nullable;
import io.reactivex.Flowable;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;
import bolts.Task;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.log.RCLog;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.DDPSubscription;

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
  public Task<DDPClientCallback.Connect> connect(@Nullable String session,
                                                 boolean usesSecureConnection) {
    final String protocol = usesSecureConnection ? "wss://" : "ws://";
    return ddpClient.connect(protocol + hostname + "/websocket", session);
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
  public Flowable<DDPSubscription.Event> getSubscriptionCallback() {
    return ddpClient.getSubscriptionCallback();
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

  /**
   * check WebSocket connectivity with ping.
   */
  public Task<Void> ping() {
    final String pingId = UUID.randomUUID().toString();
    RCLog.d("ping[%s] >", pingId);
    return ddpClient.ping(pingId)
        .continueWithTask(task -> {
          if (task.isFaulted()) {
            RCLog.d(task.getError(), "ping[%s] xxx failed xxx", pingId);
            return Task.forError(task.getError());
          } else {
            RCLog.d("pong[%s] <", pingId);
            return Task.forResult(null);
          }
        });
  }
}
