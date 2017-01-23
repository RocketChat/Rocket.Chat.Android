package chat.rocket.android.api;

import android.content.Context;
import android.util.Patterns;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.helper.CheckSum;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.internal.MethodCall;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;
import chat.rocket.android.service.DDPClientRef;
import chat.rocket.android_ddp.DDPClientCallback;
import hugo.weaving.DebugLog;

/**
 * Utility class for creating/handling MethodCall or RPC.
 *
 * TODO: separate method into several manager classes (SubscriptionManager, MessageManager, ...).
 */
public class MethodCallHelper {

  protected static final long TIMEOUT_MS = 4000;
  protected static final Continuation<String, Task<JSONObject>> CONVERT_TO_JSON_OBJECT =
      task -> Task.forResult(new JSONObject(task.getResult()));
  protected static final Continuation<String, Task<JSONArray>> CONVERT_TO_JSON_ARRAY =
      task -> Task.forResult(new JSONArray(task.getResult()));
  protected final Context context;
  protected final RealmHelper realmHelper;
  protected final DDPClientRef ddpClientRef;

  /**
   * initialize with Context and hostname.
   */
  public MethodCallHelper(Context context, String hostname) {
    this.context = context;
    this.realmHelper = RealmStore.get(hostname);
    ddpClientRef = null;
  }

  /**
   * initialize with RealmHelper and DDPClient.
   */
  public MethodCallHelper(RealmHelper realmHelper, DDPClientRef ddpClientRef) {
    this.context = null;
    this.realmHelper = realmHelper;
    this.ddpClientRef = ddpClientRef;
  }

  @DebugLog
  private Task<String> executeMethodCall(String methodName, String param, long timeout) {
    if (ddpClientRef != null) {
      return ddpClientRef.get().rpc(UUID.randomUUID().toString(), methodName, param, timeout)
          .onSuccessTask(task -> Task.forResult(task.getResult().result));
    } else {
      return MethodCall.execute(context, realmHelper, methodName, param, timeout);
    }
  }

  private Task<String> injectErrorHandler(Task<String> task) {
    return task.continueWithTask(_task -> {
      if (_task.isFaulted()) {
        Exception exception = _task.getError();
        if (exception instanceof MethodCall.Error) {
          String errMessageJson = exception.getMessage();
          if (TextUtils.isEmpty(errMessageJson)) {
            return Task.forError(exception);
          }
          String errMessage = new JSONObject(errMessageJson).getString("message");
          return Task.forError(new Exception(errMessage));
        } else if (exception instanceof DDPClientCallback.RPC.Error) {
          String errMessage = ((DDPClientCallback.RPC.Error) exception).error.getString("message");
          return Task.forError(new Exception(errMessage));
        } else if (exception instanceof DDPClientCallback.RPC.Timeout) {
          return Task.forError(new MethodCall.Timeout());
        } else {
          return Task.forError(exception);
        }
      } else {
        return _task;
      }
    });
  }

  protected final Task<String> call(String methodName, long timeout) {
    return injectErrorHandler(executeMethodCall(methodName, null, timeout));
  }

  protected final Task<String> call(String methodName, long timeout, ParamBuilder paramBuilder) {
    try {
      final JSONArray params = paramBuilder.buildParam();
      return injectErrorHandler(executeMethodCall(methodName,
          params != null ? params.toString() : null, timeout));
    } catch (JSONException exception) {
      return Task.forError(exception);
    }
  }

  /**
   * Register User.
   */
  public Task<String> registerUser(final String name, final String email,
                                   final String password, final String confirmPassword) {
    return call("registerUser", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
        .put("name", name)
        .put("email", email)
        .put("pass", password)
        .put("confirm-pass", confirmPassword))); // nothing to do.
  }

  private Task<Void> saveToken(Task<String> task) {
    return realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(Session.class, new JSONObject()
            .put("sessionId", Session.DEFAULT_ID)
            .put("token", task.getResult())
            .put("tokenVerified", true)
            .put("error", JSONObject.NULL)
        ));
  }

  /**
   * set current user's name.
   */
  public Task<String> setUsername(final String username) {
    return call("setUsername", TIMEOUT_MS, () -> new JSONArray().put(username));
  }

  public Task<Void> joinDefaultChannels() {
    return call("joinDefaultChannels", TIMEOUT_MS)
        .onSuccessTask(task -> Task.forResult(null));
  }

  /**
   * Login with username/email and password.
   */
  public Task<Void> loginWithEmail(final String usernameOrEmail, final String password) {
    return call("login", TIMEOUT_MS, () -> {
      JSONObject param = new JSONObject();
      if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
        param.put("user", new JSONObject().put("email", usernameOrEmail));
      } else {
        param.put("user", new JSONObject().put("username", usernameOrEmail));
      }
      param.put("password", new JSONObject()
          .put("digest", CheckSum.sha256(password))
          .put("algorithm", "sha-256"));
      return new JSONArray().put(param);
    }).onSuccessTask(CONVERT_TO_JSON_OBJECT)
        .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
        .onSuccessTask(this::saveToken);
  }

  /**
   * Login with OAuth.
   */
  public Task<Void> loginWithOAuth(final String credentialToken,
                                   final String credentialSecret) {
    return call("login", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
        .put("oauth", new JSONObject()
            .put("credentialToken", credentialToken)
            .put("credentialSecret", credentialSecret))
    )).onSuccessTask(CONVERT_TO_JSON_OBJECT)
        .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
        .onSuccessTask(this::saveToken);
  }

  /**
   * Login with token.
   */
  public Task<Void> loginWithToken(final String token) {
    return call("login", TIMEOUT_MS, () -> new JSONArray().put(new JSONObject()
        .put("resume", token)
    )).onSuccessTask(CONVERT_TO_JSON_OBJECT)
        .onSuccessTask(task -> Task.forResult(task.getResult().getString("token")))
        .onSuccessTask(this::saveToken)
        .continueWithTask(task -> {
          if (task.isFaulted()) {
            Session.logError(realmHelper, task.getError());
          }
          return task;
        });
  }

  /**
   * Logout.
   */
  public Task<Void> logout() {
    return call("logout", TIMEOUT_MS).onSuccessTask(task ->
        realmHelper.executeTransaction(realm -> {
          realm.delete(Session.class);
          return null;
        }));
  }

  /**
   * request "subscriptions/get".
   */
  public Task<Void> getRoomSubscriptions() {
    return call("subscriptions/get", TIMEOUT_MS).onSuccessTask(CONVERT_TO_JSON_ARRAY)
        .onSuccessTask(task -> {
          final JSONArray result = task.getResult();
          try {
            for (int i = 0; i < result.length(); i++) {
              RoomSubscription.customizeJson(result.getJSONObject(i));
            }

            return realmHelper.executeTransaction(realm -> {
              realm.delete(RoomSubscription.class);
              realm.createOrUpdateAllFromJson(
                  RoomSubscription.class, result);
              return null;
            });
          } catch (JSONException exception) {
            return Task.forError(exception);
          }
        });
  }

  /**
   * Load messages for room.
   */
  public Task<JSONArray> loadHistory(final String roomId, final long timestamp,
                                     final int count, final long lastSeen) {
    return call("loadHistory", TIMEOUT_MS, () -> new JSONArray()
        .put(roomId)
        .put(timestamp > 0 ? new JSONObject().put("$date", timestamp) : JSONObject.NULL)
        .put(count)
        .put(lastSeen > 0 ? new JSONObject().put("$date", lastSeen) : JSONObject.NULL)
    ).onSuccessTask(CONVERT_TO_JSON_OBJECT)
        .onSuccessTask(task -> {
          JSONObject result = task.getResult();
          final JSONArray messages = result.getJSONArray("messages");
          for (int i = 0; i < messages.length(); i++) {
            Message.customizeJson(messages.getJSONObject(i));
          }

          return realmHelper.executeTransaction(realm -> {
            if (timestamp == 0) {
              realm.where(Message.class)
                  .equalTo("rid", roomId)
                  .equalTo("syncstate", SyncState.SYNCED)
                  .findAll().deleteAllFromRealm();
            }
            if (messages.length() > 0) {
              realm.createOrUpdateAllFromJson(Message.class, messages);
            }
            return null;
          }).onSuccessTask(_task -> Task.forResult(messages));
        });
  }

  /**
   * update user's status.
   */
  public Task<Void> setUserStatus(final String status) {
    return call("UserPresence:setDefaultStatus", TIMEOUT_MS, () -> new JSONArray().put(status))
        .onSuccessTask(task -> Task.forResult(null));
  }

  public Task<Void> setUserPresence(final String status) {
    return call("UserPresence:" + status, TIMEOUT_MS)
        .onSuccessTask(task -> Task.forResult(null));
  }

  public Task<JSONObject> getUsersOfRoom(final String roomId, final boolean showAll) {
    return call("getUsersOfRoom", TIMEOUT_MS, () -> new JSONArray().put(roomId).put(showAll))
        .onSuccessTask(CONVERT_TO_JSON_OBJECT);
  }

  public Task<Void> createChannel(final String name, final boolean readOnly) {
    return call("createChannel", TIMEOUT_MS, () -> new JSONArray()
        .put(name)
        .put(new JSONArray())
        .put(readOnly))
        .onSuccessTask(task -> Task.forResult(null));
  }

  public Task<Void> createPrivateGroup(final String name, final boolean readOnly) {
    return call("createPrivateGroup", TIMEOUT_MS, () -> new JSONArray()
        .put(name)
        .put(new JSONArray())
        .put(readOnly))
        .onSuccessTask(task -> Task.forResult(null));
  }

  public Task<Void> createDirectMessage(final String username) {
    return call("createDirectMessage", TIMEOUT_MS, () -> new JSONArray().put(username))
        .onSuccessTask(task -> Task.forResult(null));
  }

  /**
   * send message.
   */
  public Task<JSONObject> sendMessage(String messageId, String roomId, String msg) {
    try {
      return sendMessage(new JSONObject()
          .put("_id", messageId)
          .put("rid", roomId)
          .put("msg", msg));
    } catch (JSONException exception) {
      return Task.forError(exception);
    }
  }

  /**
   * Send message object.
   */
  private Task<JSONObject> sendMessage(final JSONObject messageJson) {
    return call("sendMessage", TIMEOUT_MS, () -> new JSONArray().put(messageJson))
        .onSuccessTask(CONVERT_TO_JSON_OBJECT)
        .onSuccessTask(task -> Task.forResult(Message.customizeJson(task.getResult())));
  }

  /**
   * mark all messages are read in the room.
   */
  public Task<Void> readMessages(final String roomId) {
    return call("readMessages", TIMEOUT_MS, () -> new JSONArray().put(roomId))
        .onSuccessTask(task -> Task.forResult(null));
  }

  public Task<Void> getPublicSettings() {
    return call("public-settings/get", TIMEOUT_MS)
        .onSuccessTask(CONVERT_TO_JSON_ARRAY)
        .onSuccessTask(task -> {
          final JSONArray settings = task.getResult();
          for (int i = 0; i < settings.length(); i++) {
            PublicSetting.customizeJson(settings.getJSONObject(i));
          }

          return realmHelper.executeTransaction(realm -> {
            realm.delete(PublicSetting.class);
            realm.createOrUpdateAllFromJson(PublicSetting.class, settings);
            return null;
          });
        });
  }


  protected interface ParamBuilder {
    JSONArray buildParam() throws JSONException;
  }
}
