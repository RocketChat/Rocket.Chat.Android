package chat.rocket.android.helper;

import android.util.Patterns;
import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.ServerConfig;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class for creating/handling MethodCall.
 */
public class MethodCallHelper {

  private final String serverConfigId;

  public MethodCallHelper(String serverConfigId) {
    this.serverConfigId = serverConfigId;
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
    return injectErrorHandler(MethodCall.execute(serverConfigId, methodName, null))
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

    return injectErrorHandler(MethodCall.execute(serverConfigId, methodName, param.toString()))
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

  private Continuation<String, Task<Void>> saveToken() {
    return task -> RealmHelperBolts.executeTransaction(realm ->
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
    }, task -> Task.forResult(task.getResult().getString("token"))).onSuccessTask(saveToken());
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
        task -> Task.forResult(task.getResult().getString("token"))).onSuccessTask(saveToken());
  }

  /**
   * Login with token.
   */
  public Task<Void> loginWithToken(final String token) {
    return call("login", param -> param.put("resume", token),
        task -> Task.forResult(task.getResult().getString("token"))).onSuccessTask(saveToken());
  }

  /**
   * Logout.
   */
  public Task<Void> logout() {
    return call("logout", task -> Task.forResult(null));
  }
}
