package chat.rocket.android.helper;

import android.util.Patterns;
import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.RoomSubscription;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
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

  public MethodCallHelper(String serverConfigId) {
    this.serverConfigId = serverConfigId;
    api = null;
  }

  public MethodCallHelper(String serverConfigId, RocketChatWebSocketAPI api) {
    this.serverConfigId = serverConfigId;
    this.api = api;
  }

  private Task<JSONObject> executeMethodCall(String methodName, String param) {
    if (api != null) {
      return api.rpc(UUID.randomUUID().toString(), methodName, param)
          .onSuccessTask(task -> Task.forResult(task.getResult().result));
    } else {
      return MethodCall.execute(serverConfigId, methodName, param);
    }
  }

  private Task<JSONObject> injectErrorHandler(Task<JSONObject> task) {
    return task.continueWithTask(_task -> {
      if (_task.isFaulted()) {
        Exception exception = _task.getError();
        if (exception instanceof MethodCall.Error) {
          String errMessage = new JSONObject(exception.getMessage()).getString("message");
          return Task.forError(new Exception(errMessage));
        } else {
          return Task.forError(exception);
        }
      } else {
        return _task;
      }
    });
  }

  private interface ParamBuilder {
    void buildParam(JSONObject param) throws JSONException;
  }

  private <T> Task<T> call(String methodName,
      Continuation<JSONObject, Task<T>> onSuccess) {
    return injectErrorHandler(executeMethodCall(methodName, null))
        .onSuccessTask(onSuccess);
  }

  private <T> Task<T> call(String methodName, ParamBuilder paramBuilder,
      Continuation<JSONObject, Task<T>> onSuccess) {
    JSONObject param = new JSONObject();

    try {
      paramBuilder.buildParam(param);
    } catch (JSONException exception) {
      return Task.forError(exception);
    }

    return injectErrorHandler(executeMethodCall(methodName, param.toString()))
        .onSuccessTask(onSuccess);
  }

  /**
   * Register User.
   */
  public Task<Void> registerUser(final String name, final String email,
      final String password, final String confirmPassword) {
    return call("registerUser", param -> param
        .put("name", name)
        .put("email", email)
        .put("pass", password)
        .put("confirm-pass", confirmPassword),
        task -> Task.forResult(null)); // nothing to do.
  }

  private Task<Void> saveToken(Task<String> task) {
    return RealmHelperBolts.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(ServerConfig.class, new JSONObject()
            .put("id", serverConfigId)
            .put("token", task.getResult())
            .put("tokenVerified", true)));
  }

  /**
   * Login with username/email and password.
   */
  public Task<Void> loginWithEmail(final String usernameOrEmail, final String password) {
    return call("login", param -> {
      if (Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches()) {
        param.put("user", new JSONObject().put("email", usernameOrEmail));
      } else {
        param.put("user", new JSONObject().put("username", usernameOrEmail));
      }
      param.put("password", new JSONObject()
          .put("digest", CheckSum.sha256(password))
          .put("algorithm", "sha-256"));
    }, task -> Task.forResult(task.getResult().getString("token"))).onSuccessTask(this::saveToken);
  }

  /**
   * Login with GitHub OAuth.
   */
  public Task<Void> loginWithGitHub(final String credentialToken,
      final String credentialSecret) {
    return call("login", param -> param
        .put("oauth", new JSONObject()
            .put("credentialToken", credentialToken)
            .put("credentialSecret", credentialSecret)),
        task -> Task.forResult(task.getResult().getString("token"))).onSuccessTask(this::saveToken);
  }

  /**
   * Login with token.
   */
  public Task<Void> loginWithToken(final String token) {
    return call("login", param -> param.put("resume", token),
        task -> Task.forResult(task.getResult().getString("token"))).onSuccessTask(this::saveToken);
  }

  /**
   * Logout.
   */
  public Task<Void> logout() {
    return call("logout", task -> Task.forResult(null));
  }

  /**
   * request "subscriptions/get" and "rooms/get".
   */
  public Task<Void> getRooms() {
    return getRoomSubscriptionRecursive(0)
        .onSuccessTask(task -> getRoomRecursive(0))
        .onSuccessTask(task -> Task.forResult(null));
  }

  private Task<Long> getRoomSubscriptionRecursive(long timestamp) {
    return call("subscriptions/get", param -> param.put("$date", timestamp), task -> {
      JSONObject result = task.getResult();

      long nextTimestamp = 0;
      try {
        nextTimestamp = result.getJSONArray("remove")
            .getJSONObject(0).getJSONObject("_deletedAt").getLong("$date");
      } catch (JSONException exception) {
      }

      try {
        JSONArray updatedRooms = result.getJSONArray("update");
        for (int i = 0; i < updatedRooms.length(); i++) {
          updatedRooms.getJSONObject(i).put("serverConfigId", serverConfigId);
        }

        Task<Void> saveToDB =  RealmHelperBolts.executeTransaction(realm -> {
          realm.createOrUpdateAllFromJson(RoomSubscription.class, result.getJSONArray("update"));
          return null;
        });

        if (nextTimestamp > 0 && (timestamp == 0 || nextTimestamp < timestamp)) {
          final long _next = nextTimestamp;
          return saveToDB.onSuccessTask(_task -> getRoomSubscriptionRecursive(_next));
        } else {
          return saveToDB.onSuccessTask(_task -> Task.forResult(0L));
        }
      } catch (JSONException exception) {
        return Task.forError(exception);
      }
    });
  }

  private Task<Long> getRoomRecursive(long timestamp) {
    return call("rooms/get", param -> param.put("$date", timestamp), task -> {
      JSONObject result = task.getResult();

      long nextTimestamp = 0;
      try {
        nextTimestamp = result.getJSONArray("remove")
            .getJSONObject(0).getJSONObject("_deletedAt").getLong("$date");
      } catch (JSONException exception) {
      }

      try {
        JSONArray updatedRooms = result.getJSONArray("update");
        for (int i = 0; i < updatedRooms.length(); i++) {
          JSONObject roomJson = updatedRooms.getJSONObject(i);
          String rid = roomJson.getString("_id");
          roomJson.put("rid", rid)
              .put("serverConfigId", serverConfigId)
              .remove("_id");
        }

        Task<Void> saveToDB =  RealmHelperBolts.executeTransaction(realm -> {
          realm.createOrUpdateAllFromJson(RoomSubscription.class, result.getJSONArray("update"));
          return null;
        });

        if (nextTimestamp > 0 && (timestamp == 0 || nextTimestamp < timestamp)) {
          final long _next = nextTimestamp;
          return saveToDB.onSuccessTask(_task -> getRoomRecursive(_next));
        } else {
          return saveToDB.onSuccessTask(_task -> Task.forResult(0L));
        }
      } catch (JSONException exception) {
        return Task.forError(exception);
      }
    });
  }
}
