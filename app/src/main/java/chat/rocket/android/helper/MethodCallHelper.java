package chat.rocket.android.helper;

import android.util.Patterns;
import bolts.Task;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.ServerConfigCredential;
import chat.rocket.android_ddp.DDPClientCallback;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;

/**
 * Utility class for creating/handling MethodCall.
 */
public class MethodCallHelper {

  private static <T> Task<T> injectErrorMessageHandler(Task<T> task) {
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

  /**
   * Register User.
   */
  public static Task<Void> registerUser(String name, String email, String passwd,
      String confirmPasswd) {
    JSONObject param = new JSONObject();

    try {
      param.put("name", name).put("email", email)
          .put("pass", passwd).put("confirm-pass", confirmPasswd);
    } catch (JSONException exception) {
      return Task.forError(exception);
    }

    return injectErrorMessageHandler(MethodCall.execute("registerUser", param.toString()))
        .onSuccessTask(task -> Task.forResult(null)); // nothing to do?
  }
}
