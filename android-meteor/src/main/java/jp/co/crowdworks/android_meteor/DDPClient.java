package jp.co.crowdworks.android_meteor;

import android.support.annotation.Nullable;

import com.squareup.okhttp.OkHttpClient;

import bolts.Task;
import bolts.TaskCompletionSource;

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
}
