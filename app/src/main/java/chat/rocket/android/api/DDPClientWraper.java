package chat.rocket.android.api;

import android.support.annotation.Nullable;
import bolts.Task;
import chat.rocket.android.helper.OkHttpHelper;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.android_ddp.DDPSubscription;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import rx.Observable;
import timber.log.Timber;

/**
 * DDP client wrapper.
 */
public class DDPClientWraper {
  private final DDPClient ddpClient;
  private final String hostname;

  private DDPClientWraper(String hostname) {
    ddpClient = new DDPClient(OkHttpHelper.getClientForWebSocket());
    this.hostname = hostname;
  }

  /**
   * create new API client instance.
   */
  public static DDPClientWraper create(String hostname) {
    return new DDPClientWraper(hostname);
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
   * Execute raw RPC.
   */
  public Task<DDPClientCallback.RPC> rpc(String methodCallId, String methodName, String params,
      long timeoutMs) {
    Timber.d("rpc:[%s]> %s(%s) timeout=%d", methodCallId, methodName, params, timeoutMs);
    if (TextUtils.isEmpty(params)) {
      return ddpClient.rpc(methodName, null, methodCallId, timeoutMs).continueWithTask(task -> {
        if (task.isFaulted()) {
          Timber.d("rpc:[%s]< error = %s", methodCallId, task.getError());
        } else {
          Timber.d("rpc:[%s]< result = %s", methodCallId, task.getResult().result);
        }
        return task;
      });
    }

    try {
      return ddpClient.rpc(methodName, new JSONArray(params), methodCallId, timeoutMs)
          .continueWithTask(task -> {
            if (task.isFaulted()) {
              Timber.d("rpc:[%s]< error = %s", methodCallId, task.getError());
            } else {
              Timber.d("rpc:[%s]< result = %s", methodCallId, task.getResult().result);
            }
            return task;
          });
    } catch (JSONException exception) {
      return Task.forError(exception);
    }
  }
}
