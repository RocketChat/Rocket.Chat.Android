package chat.rocket.android_ddp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.log.RCLog;
import chat.rocket.android_ddp.rx.RxWebSocket;
import chat.rocket.android_ddp.rx.RxWebSocketCallback;
import okhttp3.OkHttpClient;
import rx.Observable;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

public class DDPClientImpl {
  private final DDPClient client;
  private final RxWebSocket websocket;
  private Observable<RxWebSocketCallback.Base> observable;
  private CompositeSubscription subscriptions;

  public DDPClientImpl(DDPClient self, OkHttpClient client) {
    websocket = new RxWebSocket(client);
    this.client = self;
  }

  private static JSONObject toJson(String s) {
    if (TextUtils.isEmpty(s)) {
      return null;
    }
    try {
      return new JSONObject(s);
    } catch (JSONException e) {
      return null;
    }
  }

  private static String extractMsg(JSONObject response) {
    if (response == null || response.isNull("msg")) {
      return null;
    } else {
      return response.optString("msg");
    }
  }

  public void connect(final TaskCompletionSource<DDPClientCallback.Connect> task, final String url,
                      String session) {
    try {
      observable = websocket.connect(url).autoConnect();
      CompositeSubscription subscriptions = new CompositeSubscription();

      subscriptions.add(observable.filter(callback -> callback instanceof RxWebSocketCallback.Open)
          .subscribe(callback -> {
            sendMessage("connect",
                json -> (TextUtils.isEmpty(session) ? json : json.put("session", session)).put(
                    "version", "pre2").put("support", new JSONArray().put("pre2").put("pre1")),
                task);
          }, err -> {
          }));

      subscriptions.add(
          observable.filter(callback -> callback instanceof RxWebSocketCallback.Message)
              .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
              .map(DDPClientImpl::toJson)
              .timeout(7, TimeUnit.SECONDS)
              .subscribe(response -> {
                String msg = extractMsg(response);
                if ("connected".equals(msg) && !response.isNull("session")) {
                  task.trySetResult(
                      new DDPClientCallback.Connect(client, response.optString("session")));
                  subscriptions.unsubscribe();
                } else if ("error".equals(msg) && "Already connected".equals(
                    response.optString("reason"))) {
                  task.trySetResult(new DDPClientCallback.Connect(client, null));
                  subscriptions.unsubscribe();
                } else if ("failed".equals(msg)) {
                  task.trySetError(
                      new DDPClientCallback.Connect.Failed(client, response.optString("version")));
                  subscriptions.unsubscribe();
                }
              }, err -> {
                task.trySetError(new DDPClientCallback.Connect.Timeout(client));
              }));

      addErrorCallback(subscriptions, task);

      subscribeBaseListeners();
    } catch (Exception e) {
      RCLog.e(e);
    }
  }

  public void ping(final TaskCompletionSource<DDPClientCallback.Ping> task,
                   @Nullable final String id) {

    final boolean requested = (TextUtils.isEmpty(id)) ?
        sendMessage("ping", null) :
        sendMessage("ping", json -> json.put("id", id));

    if (requested) {
      CompositeSubscription subscriptions = new CompositeSubscription();

      subscriptions.add(
          observable.filter(callback -> callback instanceof RxWebSocketCallback.Message)
              .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
              .map(DDPClientImpl::toJson)
              .timeout(4, TimeUnit.SECONDS)
              .subscribe(response -> {
                String msg = extractMsg(response);
                if ("pong".equals(msg)) {
                  if (response.isNull("id")) {
                    task.setResult(new DDPClientCallback.Ping(client, null));
                    subscriptions.unsubscribe();
                  } else {
                    String _id = response.optString("id");
                    if (id.equals(_id)) {
                      task.setResult(new DDPClientCallback.Ping(client, id));
                      subscriptions.unsubscribe();
                    }
                  }
                }
              }, err -> {
                task.setError(new DDPClientCallback.Ping.Timeout(client));
              }));

      addErrorCallback(subscriptions, task);
    }
  }

  public void sub(final TaskCompletionSource<DDPSubscription.Ready> task, String name,
                  JSONArray params, String id) {
    final boolean requested =
        sendMessage("sub", json -> json.put("id", id).put("name", name).put("params", params));

    if (requested) {
      CompositeSubscription subscriptions = new CompositeSubscription();

      subscriptions.add(
          observable.filter(callback -> callback instanceof RxWebSocketCallback.Message)
              .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
              .map(DDPClientImpl::toJson)
              .subscribe(response -> {
                String msg = extractMsg(response);
                if ("ready".equals(msg) && !response.isNull("subs")) {
                  JSONArray ids = response.optJSONArray("subs");
                  for (int i = 0; i < ids.length(); i++) {
                    String _id = ids.optString(i);
                    if (id.equals(_id)) {
                      task.setResult(new DDPSubscription.Ready(client, id));
                      subscriptions.unsubscribe();
                      break;
                    }
                  }
                } else if ("nosub".equals(msg) && !response.isNull("id") && !response.isNull(
                    "error")) {
                  String _id = response.optString("id");
                  if (id.equals(_id)) {
                    task.setError(new DDPSubscription.NoSub.Error(client, id,
                        response.optJSONObject("error")));
                    subscriptions.unsubscribe();
                  }
                }
              }, err -> {
              }));

      addErrorCallback(subscriptions, task);
    }
  }

  public void unsub(final TaskCompletionSource<DDPSubscription.NoSub> task,
                    @Nullable final String id) {

    final boolean requested = sendMessage("unsub", json -> json.put("id", id));

    if (requested) {
      CompositeSubscription subscriptions = new CompositeSubscription();

      subscriptions.add(
          observable.filter(callback -> callback instanceof RxWebSocketCallback.Message)
              .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
              .map(DDPClientImpl::toJson)
              .subscribe(response -> {
                String msg = extractMsg(response);
                if ("nosub".equals(msg) && response.isNull("error") && !response.isNull("id")) {
                  String _id = response.optString("id");
                  if (id.equals(_id)) {
                    task.setResult(new DDPSubscription.NoSub(client, id));
                    subscriptions.unsubscribe();
                  }
                }
              }, err -> {
              }));

      addErrorCallback(subscriptions, task);
    }
  }

  public void rpc(final TaskCompletionSource<DDPClientCallback.RPC> task, String method,
                  JSONArray params, String id, long timeoutMs) {
    final boolean requested =
        sendMessage("method",
            json -> json.put("method", method).put("params", params).put("id", id));

    if (requested) {
      CompositeSubscription subscriptions = new CompositeSubscription();

      subscriptions.add(
          observable.filter(callback -> callback instanceof RxWebSocketCallback.Message)
              .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
              .map(DDPClientImpl::toJson)
              .timeout(timeoutMs, TimeUnit.MILLISECONDS)
              .subscribe(response -> {
                String msg = extractMsg(response);
                if ("result".equals(msg)) {
                  String _id = response.optString("id");
                  if (id.equals(_id)) {
                    if (!response.isNull("error")) {
                      task.setError(new DDPClientCallback.RPC.Error(client, id,
                          response.optJSONObject("error")));
                    } else {
                      String result = response.optString("result");
                      task.setResult(new DDPClientCallback.RPC(client, id, result));
                    }
                    subscriptions.unsubscribe();
                  }
                }
              }, err -> {
                if (err instanceof TimeoutException) {
                  task.setError(new DDPClientCallback.RPC.Timeout(client));
                }
              }));

      addErrorCallback(subscriptions, task);
    }
  }

  private void subscribeBaseListeners() {
    if (subscriptions != null &&
        subscriptions.hasSubscriptions() && !subscriptions.isUnsubscribed()) {
      return;
    }

    subscriptions = new CompositeSubscription();
    subscriptions.add(
        observable.filter(callback -> callback instanceof RxWebSocketCallback.Message)
            .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
            .map(DDPClientImpl::toJson)
            .subscribe(response -> {
              String msg = extractMsg(response);
              if ("ping".equals(msg)) {
                if (response.isNull("id")) {
                  sendMessage("pong", null);
                } else {
                  sendMessage("pong", json -> json.put("id", response.getString("id")));
                }
              }
            }, err -> {
            }));
  }

  public Observable<DDPSubscription.Event> getDDPSubscription() {
    String[] targetMsgs = {"added", "changed", "removed", "addedBefore", "movedBefore"};
    return observable.filter(callback -> callback instanceof RxWebSocketCallback.Message)
        .map(callback -> ((RxWebSocketCallback.Message) callback).responseBodyString)
        .map(DDPClientImpl::toJson)
        .filter(response -> {
          String msg = extractMsg(response);
          for (String m : targetMsgs) {
            if (m.equals(msg)) {
              return true;
            }
          }
          return false;
        })
        .map((Func1<JSONObject, DDPSubscription.Event>) response -> {
          String msg = extractMsg(response);
          if ("added".equals(msg)) {
            return new DDPSubscription.Added(client, response.optString("collection"),
                response.optString("id"),
                response.isNull("fields") ? null : response.optJSONObject("fields"));
          } else if ("addedBefore".equals(msg)) {
            return new DDPSubscription.Added.Before(client, response.optString("collection"),
                response.optString("id"),
                response.isNull("fields") ? null : response.optJSONObject("fields"),
                response.isNull("before") ? null : response.optString("before"));
          } else if ("changed".equals(msg)) {
            return new DDPSubscription.Changed(client, response.optString("collection"),
                response.optString("id"),
                response.isNull("fields") ? null : response.optJSONObject("fields"),
                response.isNull("cleared") ? new JSONArray() : response.optJSONArray("before"));
          } else if ("removed".equals(msg)) {
            return new DDPSubscription.Removed(client, response.optString("collection"),
                response.optString("id"));
          } else if ("movedBefore".equals(msg)) {
            return new DDPSubscription.MovedBefore(client, response.optString("collection"),
                response.optString("id"),
                response.isNull("before") ? null : response.optString("before"));
          }

          return null;
        })
        .asObservable();
  }

  public void unsubscribeBaseListeners() {
    if (subscriptions.hasSubscriptions() && !subscriptions.isUnsubscribed()) {
      subscriptions.unsubscribe();
    }
  }

  public Task<RxWebSocketCallback.Close> getOnCloseCallback() {
    TaskCompletionSource<RxWebSocketCallback.Close> task = new TaskCompletionSource<>();

    observable.filter(callback -> callback instanceof RxWebSocketCallback.Close)
        .cast(RxWebSocketCallback.Close.class)
        .subscribe(task::setResult, err -> {
          if (err instanceof Exception) {
            task.setError((Exception) err);
          } else {
            task.setError(new Exception(err));
          }
        });

    return task.getTask().onSuccessTask(_task -> {
      unsubscribeBaseListeners();
      return _task;
    });
  }

  private boolean sendMessage(String msg, @Nullable JSONBuilder json) {
    try {
      JSONObject origJson = new JSONObject().put("msg", msg);
      String msg2 = (json == null ? origJson : json.create(origJson)).toString();
      return websocket.sendText(msg2);
    } catch (Exception e) {
      RCLog.e(e);
    }
    return true; // ignore exception here.
  }

  private void sendMessage(String msg, @Nullable JSONBuilder json,
                              TaskCompletionSource<?> taskForSetError) {
    if (!sendMessage(msg, json)) {
      taskForSetError.trySetError(new DDPClientCallback.Closed(client));
    }
  }

  private void addErrorCallback(CompositeSubscription subscriptions, TaskCompletionSource<?> task) {
    subscriptions.add(observable.subscribe(base -> {
    }, err -> {
      task.trySetError(new Exception(err));
      subscriptions.unsubscribe();
    }));
  }

  public void close(int code, String reason) {
    try {
      websocket.close(code, reason);
    } catch (Exception e) {
      RCLog.e(e);
    }
  }

  private interface JSONBuilder {
    @NonNull
    JSONObject create(JSONObject root) throws JSONException;
  }
}
