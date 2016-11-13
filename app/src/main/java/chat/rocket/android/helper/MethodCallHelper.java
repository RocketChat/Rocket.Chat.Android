package chat.rocket.android.helper;

import android.util.Patterns;
import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import chat.rocket.android_ddp.DDPClientCallback;
import java.util.UUID;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for creating/handling MethodCall or RPC.
 */
public class MethodCallHelper {

  private final String serverConfigId;
  private final RocketChatWebSocketAPI api;
  private static final long TIMEOUT_MS = 4000;

  public MethodCallHelper(String serverConfigId) {
    this.serverConfigId = serverConfigId;
    api = null;
  }

  public MethodCallHelper(String serverConfigId, RocketChatWebSocketAPI api) {
    this.serverConfigId = serverConfigId;
    this.api = api;
  }

  private Task<String> executeMethodCall(String methodName, String param, long timeout) {
    if (api != null) {
      return api.rpc(UUID.randomUUID().toString(), methodName, param, timeout)
          .onSuccessTask(task -> Task.forResult(task.getResult().result));
    } else {
      return MethodCall.execute(serverConfigId, methodName, param, timeout);
    }
  }

  private Task<String> injectErrorHandler(Task<String> task) {
    return task.continueWithTask(_task -> {
      if (_task.isFaulted()) {
        Exception exception = _task.getError();
        if (exception instanceof MethodCall.Error) {
          String errMessage = new JSONObject(exception.getMessage()).getString("message");
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
    return RealmHelperBolts.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
            .put("serverConfigId", serverConfigId)
            .put("token", task.getResult())
            .put("tokenVerified", true)));
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
              result.getJSONObject(i).put("serverConfigId", serverConfigId);
            }

            return RealmHelperBolts.executeTransaction(realm -> {
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

          return RealmHelperBolts.executeTransaction(realm -> {
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
