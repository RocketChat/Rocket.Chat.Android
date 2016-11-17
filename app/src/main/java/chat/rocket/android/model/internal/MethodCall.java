package chat.rocket.android.model.internal;

import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.UUID;
import org.json.JSONObject;
import timber.log.Timber;

public class MethodCall extends RealmObject {

  @PrimaryKey private String methodCallId;
  private int syncstate;
  private String name;
  private String paramsJson;
  private String resultJson;
  private long timeout;

  public String getMethodCallId() {
    return methodCallId;
  }

  public void setMethodCallId(String methodCallId) {
    this.methodCallId = methodCallId;
  }

  public int getSyncstate() {
    return syncstate;
  }

  public void setSyncstate(int syncstate) {
    this.syncstate = syncstate;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getParamsJson() {
    return paramsJson;
  }

  public void setParamsJson(String paramsJson) {
    this.paramsJson = paramsJson;
  }

  public String getResultJson() {
    return resultJson;
  }

  public void setResultJson(String resultJson) {
    this.resultJson = resultJson;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public static class Error extends Exception {
    public Error(String message) {
      super(message);
    }

    public Error(Throwable exception) {
      super(exception);
    }
  }

  public static class Timeout extends Exception {
    public Timeout() {
      super("MethodCall.Timeout");
    }
  }

  /**
   * insert a new record to request a method call.
   */
  public static Task<String> execute(RealmHelper realmHelper, String name, String paramsJson,
      long timeout) {
    final String newId = UUID.randomUUID().toString();
    TaskCompletionSource<String> task = new TaskCompletionSource<>();
    realmHelper.executeTransaction(realm -> {
      MethodCall call = realm.createObjectFromJson(MethodCall.class, new JSONObject()
          .put("methodCallId", newId)
          .put("syncstate", SyncState.NOT_SYNCED)
          .put("timeout", timeout)
          .put("name", name));
      call.setParamsJson(paramsJson);
      return null;
    }).continueWith(_task -> {
      if (_task.isFaulted()) {
        task.setError(_task.getError());
      } else {
        final RealmObjectObserver<MethodCall> observer =
            realmHelper.createObjectObserver(realm ->
                realm.where(MethodCall.class).equalTo("methodCallId", newId));
        observer.setOnUpdateListener(methodCall -> {
          int syncstate = methodCall.getSyncstate();
          Timber.d("MethodCall[%s] syncstate=%d", methodCall.getMethodCallId(), syncstate);
          if (syncstate == SyncState.SYNCED) {
            String resultJson = methodCall.getResultJson();
            if (TextUtils.isEmpty(resultJson)) {
              task.setResult(null);
            }
            task.setResult(resultJson);
            observer.unsub();
            remove(realmHelper, methodCall.getMethodCallId()).continueWith(new LogcatIfError());
          } else if (syncstate == SyncState.FAILED) {
            task.setError(new Error(methodCall.getResultJson()));
            observer.unsub();
            remove(realmHelper, methodCall.getMethodCallId()).continueWith(new LogcatIfError());
          }
        });
        observer.sub();
      }
      return null;
    });
    return task.getTask();
  }

  /**
   * remove the request.
   */
  public static final Task<Void> remove(RealmHelper realmHelper, String methodCallId) {
    return realmHelper.executeTransaction(realm ->
        realm.where(MethodCall.class)
            .equalTo("methodCallId", methodCallId)
            .findAll()
            .deleteAllFromRealm());
  }
}
