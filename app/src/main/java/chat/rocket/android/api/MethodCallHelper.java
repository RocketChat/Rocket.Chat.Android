package chat.rocket.android.api;

import android.util.Patterns;
import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.helper.CheckSum;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.model.internal.MethodCall;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;
import chat.rocket.android_ddp.DDPClientCallback;
import hugo.weaving.DebugLog;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for creating/handling MethodCall or RPC.
 * TODO: separate method into several manager classes (SubscriptionManager, MessageManager, ...).
 */
public class MethodCallHelper {

  private final RealmHelper realmHelper;
  private final DDPClientWraper ddpClient;
  private static final long TIMEOUT_MS = 4000;

  public MethodCallHelper(String serverConfigId) {
    this.realmHelper = RealmStore.get(serverConfigId);
    ddpClient = null;
  }

  public MethodCallHelper(RealmHelper realmHelper, DDPClientWraper ddpClient) {
    this.realmHelper = realmHelper;
    this.ddpClient = ddpClient;
  }

  @DebugLog
  private Task<String> executeMethodCall(String methodName, String param, long timeout) {
    if (ddpClient != null) {
      return ddpClient.rpc(UUID.randomUUID().toString(), methodName, param, timeout)
          .onSuccessTask(task -> Task.forResult(task.getResult().result));
    } else {
      return MethodCall.execute(realmHelper, methodName, param, timeout);
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

  private interface ParamBuilder {
    JSONArray buildParam() throws JSONException;
  }

  private Task<String> call(String methodName, long timeout) {
    return injectErrorHandler(executeMethodCall(methodName, null, timeout));
  }

  private Task<String> call(String methodName, long timeout, ParamBuilder paramBuilder) {
    try {
      final JSONArray params = paramBuilder.buildParam();
      return injectErrorHandler(executeMethodCall(methodName,
          params != null ? params.toString() : null, timeout));
    } catch (JSONException exception) {
      return Task.forError(exception);
    }
  }

  private static final Continuation<String, Task<JSONObject>> CONVERT_TO_JSON_OBJECT =
      task -> Task.forResult(new JSONObject(task.getResult()));

  private static final Continuation<String, Task<JSONArray>> CONVERT_TO_JSON_ARRAY =
      task -> Task.forResult(new JSONArray(task.getResult()));

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
   * Login with GitHub OAuth.
   */
  public Task<Void> loginWithGitHub(final String credentialToken,
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
        .onSuccessTask(this::saveToken);
  }

  /**
   * Logout.
   */
  public Task<String> logout() {
    return call("logout", TIMEOUT_MS);
  }

  /**
   * request "subscriptions/get".
   */
  public Task<Void> getRooms() {
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
  public Task<Void> loadHistory(final String roomId, final long timestamp,
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
              realm.where(Message.class).equalTo("rid", roomId).findAll().deleteAllFromRealm();
            }
            if (messages.length() > 0) {
              realm.createOrUpdateAllFromJson(Message.class, messages);
            }
            return null;
          });
        });
  }

}
