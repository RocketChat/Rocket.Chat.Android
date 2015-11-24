package jp.co.crowdworks.android_meteor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import bolts.TaskCompletionSource;
import jp.co.crowdworks.android_meteor.rx.RxWebSocket;
import jp.co.crowdworks.android_meteor.rx.RxWebSocketCallback;
import rx.Observable;
import rx.subscriptions.CompositeSubscription;

public class DDPClientImpl {
    private final static String TAG = DDPClient.TAG;
    private final DDPClient mClient;
    private final RxWebSocket mWebSocket;
    private Observable<RxWebSocketCallback.Base> mObservable;
    private CompositeSubscription mBaseSubscriptions;

    public DDPClientImpl(DDPClient self, OkHttpClient client){
        mWebSocket = new RxWebSocket(client);
        mClient = self;
    }

    public void connect(final TaskCompletionSource<DDPClientCallback.Connect> task, final String url) {
        connect(task, url, null);
    }

    public void connect(final TaskCompletionSource<DDPClientCallback.Connect> task, final String url, String session){
        mObservable = mWebSocket.connect(url).autoConnect();
        CompositeSubscription subscriptions = new CompositeSubscription();

        subscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Open)
                .subscribe(callback -> {
                    sendMessage("connect", json ->
                            (TextUtils.isEmpty(session) ? json : json.put("session", session))
                                    .put("version", "pre1")
                                    .put("support", new JSONArray()
                                            .put("pre1")//.put("pre2").put("1")
                                    ));
                }));

        subscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Failure)
                .subscribe(callback -> {
                    task.setError(new Exception(callback.toString()));
                    subscriptions.unsubscribe();
                }));

        subscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Message)
                .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
                .map(DDPClientImpl::toJson)
                .subscribe(response -> {
                    String msg = extractMsg(response);
                    if ("connected".equals(msg) && !response.isNull("session")) {
                        task.setResult(new DDPClientCallback.Connect(mClient, response.optString("session")));
                        subscriptions.unsubscribe();
                    } else if ("failed".equals(msg)) {
                        task.setError(new DDPClientCallback.Connect.Failed(mClient, response.optString("version")));
                        subscriptions.unsubscribe();
                    }
                }));

        subscribeBaseListeners();
    }

    public void ping(final TaskCompletionSource<DDPClientCallback.Ping> task, @Nullable final String id){
        Log.w(TAG, "ping is not supported in DDP-pre1 spec.");
        CompositeSubscription subscriptions = new CompositeSubscription();

        subscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Message)
                .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
                .map(DDPClientImpl::toJson)
                .timeout(4, TimeUnit.SECONDS)
                .subscribe(response -> {
                    String msg = extractMsg(response);
                    if ("pong".equals(msg)) {
                        if (response.isNull("id")) {
                            task.setResult(new DDPClientCallback.Ping(mClient, null));
                        } else {
                            task.setResult(new DDPClientCallback.Ping(mClient, response.optString("id")));
                        }
                    }

                    subscriptions.unsubscribe();
                }, err -> {
                    task.setError(new DDPClientCallback.Ping.Timeout(mClient));
                }));

        if(TextUtils.isEmpty(id)) sendMessage("ping", null);
        else sendMessage("ping", json -> json.put("id", id));
    }

    private void subscribeBaseListeners() {
        if(mBaseSubscriptions != null &&
                mBaseSubscriptions.hasSubscriptions() && !mBaseSubscriptions.isUnsubscribed()) return;

        mBaseSubscriptions = new CompositeSubscription();
        mBaseSubscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Message)
                .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
                .map(DDPClientImpl::toJson)
                .subscribe(response -> {
                    String msg = extractMsg(response);
                    if ("ping".equals(msg)) {
                        if(response.isNull("id")) {
                            sendMessage("pong", null);
                        } else {
                            sendMessage("pong", json -> json.put("id",response.getString("id")));
                        }
                    }
                }));

        // just for debugging.
        mBaseSubscriptions.add(mObservable.subscribe(callback -> {
            Log.d(TAG, "DEBUG< " + callback);
        }));

    }

    public void unscribeBaseListeners() {
        if(mBaseSubscriptions.hasSubscriptions() && !mBaseSubscriptions.isUnsubscribed()) {
            mBaseSubscriptions.unsubscribe();
        }
    }

    private static JSONObject toJson(String s) {
        if (TextUtils.isEmpty(s)) return null;
        try {
            return new JSONObject(s);
        } catch (JSONException e) {
            return null;
        }
    }

    private static String extractMsg(JSONObject response) {
        if (response == null || response.isNull("msg")) return null;
        else return response.optString("msg");
    }

    private interface JSONBuilder {
        @NonNull
        JSONObject create(JSONObject root) throws JSONException;
    }

    private void sendMessage(String msg, @Nullable JSONBuilder json) {
        try {
            JSONObject origJson = new JSONObject().put("msg", msg);
            String msg2 = (json==null ? origJson : json.create(origJson)).toString();
            mWebSocket.sendText(msg2);
            Log.d(TAG, "DEBUG> "+msg2);
        }
        catch (Exception e) {
            Log.e(TAG, "error", e);
        }
    }
}
