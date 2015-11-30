package jp.co.crowdworks.android_ddp.ddp;

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
import jp.co.crowdworks.android_ddp.rx.RxWebSocket;
import jp.co.crowdworks.android_ddp.rx.RxWebSocketCallback;
import rx.Observable;
import rx.functions.Func1;
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
                                    .put("version", "pre2")
                                    .put("support", new JSONArray()
                                                    .put("pre2").put("pre1")
                                    ));
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
                    } else if ("error".equals(msg) && "Already connected".equals(response.optString("reason"))){
                        task.setResult(new DDPClientCallback.Connect(mClient, null));
                        subscriptions.unsubscribe();
                    } else if ("failed".equals(msg)) {
                        task.setError(new DDPClientCallback.Connect.Failed(mClient, response.optString("version")));
                        subscriptions.unsubscribe();

                    }
                }));

        addErrorCallback(subscriptions, task);

        subscribeBaseListeners();
    }

    public void ping(final TaskCompletionSource<DDPClientCallback.Ping> task, @Nullable final String id){
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
                            subscriptions.unsubscribe();
                        } else {
                            String _id = response.optString("id");
                            if (id.equals(_id)) {
                                task.setResult(new DDPClientCallback.Ping(mClient, id));
                                subscriptions.unsubscribe();
                            }
                        }
                    }
                }, err -> {
                    task.setError(new DDPClientCallback.Ping.Timeout(mClient));
                }));

        addErrorCallback(subscriptions, task);

        if(TextUtils.isEmpty(id)) sendMessage("ping", null);
        else sendMessage("ping", json -> json.put("id", id));
    }

    public void sub(final TaskCompletionSource<DDPSubscription.Ready> task, String name, JSONArray params, String id){
        CompositeSubscription subscriptions = new CompositeSubscription();

        subscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Message)
                .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
                .map(DDPClientImpl::toJson)
                .subscribe(response -> {
                    String msg = extractMsg(response);
                    if ("ready".equals(msg) && !response.isNull("subs")) {
                        JSONArray ids = response.optJSONArray("subs");
                        for(int i=0;i<ids.length();i++) {
                            String _id = ids.optString(i);
                            if(id.equals(_id)) {
                                task.setResult(new DDPSubscription.Ready(mClient, id));
                                subscriptions.unsubscribe();
                                break;
                            }
                        }
                    }
                    else if ("nosub".equals(msg) && !response.isNull("id") && !response.isNull("error")) {
                        String _id = response.optString("id");
                        if(id.equals(_id)) {
                            task.setError(new DDPSubscription.NoSub.Error(mClient, id, response.optJSONObject("error")));
                            subscriptions.unsubscribe();
                        }
                    }
                }));

        addErrorCallback(subscriptions, task);

        sendMessage("sub", json -> json
                .put("id", id)
                .put("name", name)
                .put("params", params));
    }

    public void unsub(final TaskCompletionSource<DDPSubscription.NoSub> task, @Nullable final String id){
        CompositeSubscription subscriptions = new CompositeSubscription();

        subscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Message)
                .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
                .map(DDPClientImpl::toJson)
                .subscribe(response -> {
                    String msg = extractMsg(response);
                    if ("nosub".equals(msg) && response.isNull("error") && !response.isNull("id")) {
                        String _id = response.optString("id");
                        if (id.equals(_id)) {
                            task.setResult(new DDPSubscription.NoSub(mClient, id));
                            subscriptions.unsubscribe();
                        }
                    }
                }));

        addErrorCallback(subscriptions, task);

        sendMessage("unsub", json -> json
                .put("id", id));
    }

    public void rpc(final TaskCompletionSource<DDPClientCallback.RPC> task, String method, JSONArray params, String id){
        CompositeSubscription subscriptions = new CompositeSubscription();

        subscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Message)
                .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
                .map(DDPClientImpl::toJson)
                .subscribe(response -> {
                    String msg = extractMsg(response);
                    if ("result".equals(msg)) {
                        String _id = response.optString("id");
                        if(id.equals(_id)) {
                            if (!response.isNull("error")) {
                                task.setError(new DDPClientCallback.RPC.Error(mClient, id, response.optJSONObject("error")));
                            }
                            else {
                                task.setResult(new DDPClientCallback.RPC(mClient, id,
                                        response.isNull("result")? new JSONObject() : response.optJSONObject("result")));
                            }
                            subscriptions.unsubscribe();
                        }
                    }
                }));

        addErrorCallback(subscriptions, task);

        sendMessage("method", json -> json
                        .put("method", method)
                        .put("params", params)
                        .put("id", id));
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

    public Observable<Void> getFailureObservable() {
        return mObservable.filter(callback -> callback instanceof RxWebSocketCallback.Failure)
                .map(event -> (Void)null)
                .asObservable();
    }

    public Observable<DDPSubscription.Event> getDDPSubscription() {
        String[] targetMsgs = {"added", "changed", "removed", "addedBefore", "movedBefore"};
        return mObservable.filter(callback -> callback instanceof RxWebSocketCallback.Message)
                .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
                .map(DDPClientImpl::toJson)
                .filter(response -> {
                    String msg = extractMsg(response);
                    for(String m: targetMsgs) {
                        if (m.equals(msg)) return true;
                    }
                    return false;
                }).map(
                new Func1<JSONObject, DDPSubscription.Event>() { //lambda is difficult to debug with Breakpoint...
                    @Override
                    public DDPSubscription.Event call(JSONObject response) {
                        String msg = extractMsg(response);
                        if ("added".equals(msg)) {
                            return new DDPSubscription.Added(mClient,
                                    response.optString("collection"),
                                    response.optString("id"),
                                    response.isNull("fields")? null : response.optJSONObject("fields"));
                        }
                        else if ("addedBefore".equals(msg)) {
                            return new DDPSubscription.Added.Before(mClient,
                                    response.optString("collection"),
                                    response.optString("id"),
                                    response.isNull("fields")? null : response.optJSONObject("fields"),
                                    response.isNull("before")? null : response.optString("before"));
                        }
                        else if ("changed".equals(msg)) {
                            return new DDPSubscription.Changed(mClient,
                                    response.optString("collection"),
                                    response.optString("id"),
                                    response.isNull("fields")? null : response.optJSONObject("fields"),
                                    response.isNull("cleared")? new JSONArray() : response.optJSONArray("before"));
                        }
                        else if ("removed".equals(msg)) {
                            return new DDPSubscription.Removed(mClient,
                                    response.optString("collection"),
                                    response.optString("id"));
                        }
                        else if ("movedBefore".equals(msg)) {
                            return new DDPSubscription.MovedBefore(mClient,
                                    response.optString("collection"),
                                    response.optString("id"),
                                    response.isNull("before")? null : response.optString("before"));
                        }

                        return null;
                    }
                }).asObservable();
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

    private void addErrorCallback(CompositeSubscription subscriptions, TaskCompletionSource<?> task) {
        subscriptions.add(mObservable
                .filter(callback -> callback instanceof RxWebSocketCallback.Failure)
                .subscribe(callback -> {
                    task.setError(new Exception(callback.toString()));
                    subscriptions.unsubscribe();
                }));
    }

}
