package chat.rocket.android_ddp;


import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.log.RCLog;
import chat.rocket.android_ddp.rx.RxWebSocketCallback;
import io.reactivex.Flowable;
import io.reactivex.Maybe;
import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import okhttp3.OkHttpClient;

public class DDPClient {
    // reference: https://github.com/eddflrs/meteor-ddp/blob/master/meteor-ddp.js
    public static final int REASON_CLOSED_BY_USER = 1000;
    public static final int REASON_NETWORK_ERROR = 1001;

    private static volatile DDPClient singleton;
    private static volatile OkHttpClient client;
    private final DDPClientImpl impl;
    private final AtomicReference<String> hostname = new AtomicReference<>();

    public static void initialize(OkHttpClient okHttpClient) {
        client = okHttpClient;
    }

    public static DDPClient get() {
        DDPClient result = singleton;
        if (result == null) {
            synchronized (DDPClient.class) {
                result = singleton;
                if (result == null) {
                    singleton = result = new DDPClient(client);
                }
            }
        }
        return result;
    }

    private DDPClient(OkHttpClient client) {
        impl = new DDPClientImpl(this, client);
    }

    private Task<DDPClientCallback.Connect> connect(String url, String session) {
        hostname.set(url);
        TaskCompletionSource<DDPClientCallback.Connect> task = new TaskCompletionSource<>();
        impl.connect(task, url, session);
        return task.getTask();
    }

    private Task<DDPClientCallback.Ping> ping(@Nullable String id) {
        TaskCompletionSource<DDPClientCallback.Ping> task = new TaskCompletionSource<>();
        impl.ping(task, id);
        return task.getTask();
    }

    private Maybe<DDPClientCallback.Base> doPing(@Nullable String id) {
        return impl.ping(id);
    }

    private Task<DDPSubscription.Ready> sub(String id, String name, JSONArray params) {
        TaskCompletionSource<DDPSubscription.Ready> task = new TaskCompletionSource<>();
        impl.sub(task, name, params, id);
        return task.getTask();
    }

    private Task<DDPSubscription.NoSub> unsub(String id) {
        TaskCompletionSource<DDPSubscription.NoSub> task = new TaskCompletionSource<>();
        impl.unsub(task, id);
        return task.getTask();
    }

    public Task<RxWebSocketCallback.Close> getOnCloseCallback() {
        return impl.getOnCloseCallback();
    }

    public void close() {
        impl.close(REASON_CLOSED_BY_USER, "closed by DDPClient#close()");
    }

    /**
     * check WebSocket connectivity with ping.
     */
    public Task<Void> ping() {
        final String pingId = UUID.randomUUID().toString();
        RCLog.d("ping[%s] >", pingId);
        return ping(pingId)
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

    /**
     * check WebSocket connectivity with ping.
     */
    public Maybe<DDPClientCallback.Base> doPing() {
        final String pingId = UUID.randomUUID().toString();
        RCLog.d("ping[%s] >", pingId);
        return doPing(pingId);
    }

    /**
     * Connect to WebSocket server with DDP client.
     */
    public Task<DDPClientCallback.Connect> connect(@NonNull String hostname, @Nullable String session,
                                                   boolean usesSecureConnection) {
        final String protocol = usesSecureConnection ? "wss://" : "ws://";
        return connect(protocol + hostname + "/websocket", session);
    }

    /**
     * Subscribe with DDP client.
     */
    public Task<DDPSubscription.Ready> subscribe(final String name, JSONArray param) {
        final String subscriptionId = UUID.randomUUID().toString();
        RCLog.d("sub:[%s]> %s(%s)", subscriptionId, name, param);
        return sub(subscriptionId, name, param);
    }

    /**
     * Unsubscribe with DDP client.
     */
    public Task<DDPSubscription.NoSub> unsubscribe(final String subscriptionId) {
        RCLog.d("unsub:[%s]>", subscriptionId);
        return unsub(subscriptionId);
    }

    /**
     * Returns Observable for handling DDP subscription.
     */
    public Flowable<DDPSubscription.Event> getSubscriptionCallback() {
        return impl.getDDPSubscription();
    }

    /**
     * Execute raw RPC.
     */
    public Task<DDPClientCallback.RPC> rpc(String methodCallId, String methodName, String params,
                                           long timeoutMs) {
        TaskCompletionSource<DDPClientCallback.RPC> task = new TaskCompletionSource<>();
        RCLog.d("rpc:[%s]> %s(%s) timeout=%d", methodCallId, methodName, params, timeoutMs);
        if (TextUtils.isEmpty(params)) {
            impl.rpc(task, methodName, null, methodCallId, timeoutMs);
            return task.getTask().continueWithTask(task_ -> {
                if (task_.isFaulted()) {
                    RCLog.d("rpc:[%s]< error = %s", methodCallId, task_.getError());
                } else {
                    RCLog.d("rpc:[%s]< result = %s", methodCallId, task_.getResult().result);
                }
                return task_;
            });
        }

        try {
            impl.rpc(task, methodName, new JSONArray(params), methodCallId, timeoutMs);
            return task.getTask().continueWithTask(task_ -> {
                if (task_.isFaulted()) {
                    RCLog.d("rpc:[%s]< error = %s", methodCallId, task_.getError());
                } else {
                    RCLog.d("rpc:[%s]< result = %s", methodCallId, task_.getResult().result);
                }
                return task_;
            });
        } catch (JSONException exception) {
            return Task.forError(exception);
        }
    }
}
