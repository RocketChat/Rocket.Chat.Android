package chat.rocket.android_ddp;

import android.support.annotation.Nullable;

import org.json.JSONArray;

import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android_ddp.rx.RxWebSocketCallback;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import okhttp3.OkHttpClient;

public class DDPClient {
  // reference: https://github.com/eddflrs/meteor-ddp/blob/master/meteor-ddp.js

  private final DDPClientImpl impl;

  public DDPClient(OkHttpClient client) {
    impl = new DDPClientImpl(this, client);
  }

  public Task<DDPClientCallback.Connect> connect(String url) {
    return connect(url, null);
  }

  public Task<DDPClientCallback.Connect> connect(String url, String session) {
    TaskCompletionSource<DDPClientCallback.Connect> task = new TaskCompletionSource<>();
    impl.connect(task, url, session);
    return task.getTask();
  }

  public Task<DDPClientCallback.Ping> ping(@Nullable String id) {
    TaskCompletionSource<DDPClientCallback.Ping> task = new TaskCompletionSource<>();
    impl.ping(task, id);
    return task.getTask();
  }

  public Maybe<DDPClientCallback.Base> doPing(@Nullable String id) {
    return impl.ping(id);
  }

  public Task<DDPClientCallback.RPC> rpc(String method, JSONArray params, String id,
                                         long timeoutMs) {
    TaskCompletionSource<DDPClientCallback.RPC> task = new TaskCompletionSource<>();
    impl.rpc(task, method, params, id, timeoutMs);
    return task.getTask();
  }

  public Task<DDPSubscription.Ready> sub(String id, String name, JSONArray params) {
    TaskCompletionSource<DDPSubscription.Ready> task = new TaskCompletionSource<>();
    impl.sub(task, name, params, id);
    return task.getTask();
  }

  public Task<DDPSubscription.NoSub> unsub(String id) {
    TaskCompletionSource<DDPSubscription.NoSub> task = new TaskCompletionSource<>();
    impl.unsub(task, id);
    return task.getTask();
  }

  public Flowable<DDPSubscription.Event> getSubscriptionCallback() {
    return impl.getDDPSubscription();
  }

  public Task<RxWebSocketCallback.Close> getOnCloseCallback() {
    return impl.getOnCloseCallback();
  }

  public void close() {
    impl.close(1000, "closed by DDPClient#close()");
  }
}
