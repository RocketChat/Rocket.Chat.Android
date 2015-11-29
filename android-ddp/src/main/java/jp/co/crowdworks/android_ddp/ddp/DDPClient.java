package jp.co.crowdworks.android_ddp.ddp;

import android.support.annotation.Nullable;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;

import bolts.Task;
import bolts.TaskCompletionSource;
import rx.Observable;

public class DDPClient {
    static final String TAG = "DDP";
    // reference: https://github.com/eddflrs/meteor-ddp/blob/master/meteor-ddp.js

    private final DDPClientImpl mImpl;
    public DDPClient(OkHttpClient client) {
        mImpl = new DDPClientImpl(this, client);
    }

    public Task<DDPClientCallback.Connect> connect(String url) {
        TaskCompletionSource<DDPClientCallback.Connect> task = new TaskCompletionSource<>();
        mImpl.connect(task, url);
        return task.getTask();
    }

    public Task<DDPClientCallback.Ping> ping(@Nullable String id) {
        TaskCompletionSource<DDPClientCallback.Ping> task = new TaskCompletionSource<>();
        mImpl.ping(task, id);
        return task.getTask();
    }

    public Task<DDPClientCallback.RPC> rpc(String method, JSONArray params, String id) {
        TaskCompletionSource<DDPClientCallback.RPC> task = new TaskCompletionSource<>();
        mImpl.rpc(task, method, params, id);
        return task.getTask();
    }

    public Task<DDPSubscription.Ready> sub(String id, String name, JSONArray params) {
        TaskCompletionSource<DDPSubscription.Ready> task = new TaskCompletionSource<>();
        mImpl.sub(task, name, params,id);
        return task.getTask();
    }

    public Task<DDPSubscription.NoSub> unsub(String id) {
        TaskCompletionSource<DDPSubscription.NoSub> task = new TaskCompletionSource<>();
        mImpl.unsub(task, id);
        return task.getTask();
    }

    public Observable<Void> getFailureObservable() {
        return mImpl.getFailureObservable();
    }

    public Observable<DDPSubscription.Event> getSubscriptionCallback() {
        return mImpl.getDDPSubscription();
    }
}
