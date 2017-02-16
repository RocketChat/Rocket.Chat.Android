package chat.rocket.android.helper;

import com.google.firebase.crash.FirebaseCrash;

import bolts.Continuation;
import bolts.Task;
import chat.rocket.android.BuildConfig;
import chat.rocket.android.log.RCLog;

/**
 * Bolts-Task continuation for just logging if error occurred.
 */
public class LogIfError implements Continuation {
  @Override
  public Object then(Task task) throws Exception {
    if (task.isFaulted()) {
      if (BuildConfig.DEBUG) {
        RCLog.w(task.getError());
      }
      FirebaseCrash.report(task.getError());
    }
    return task;
  }
}
