package chat.rocket.persistence.realm.models.internal;

import android.text.TextUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.UUID;
import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmObjectObserver;
import chat.rocket.persistence.realm.helpers.LogcatIfError;

public class MethodCall extends RealmObject {

  @SuppressWarnings({"PMD.ShortVariable"})
  public static final String ID = "methodCallId";
  public static final String SYNC_STATE = "syncstate";
  public static final String NAME = "name";
  public static final String PARAMS_JSON = "paramsJson";
  public static final String RESULT_JSON = "resultJson";
  public static final String TIMEOUT = "timeout";

  private static final HashMap<String, RealmObjectObserver<MethodCall>> REF_MAP = new HashMap<>();
  @PrimaryKey private String methodCallId;
  private int syncstate;
  private String name;
  private String paramsJson;
  private String resultJson;
  private long timeout;

  /**
   * insert a new record to request a method call.
   */
  public static Task<String> execute(RealmHelper realmHelper, String name, String paramsJson,
                                     long timeout) {
    final String newId = UUID.randomUUID().toString();
    TaskCompletionSource<String> task = new TaskCompletionSource<>();
    realmHelper.executeTransaction(realm -> {
      MethodCall call = realm.createObjectFromJson(MethodCall.class, new JSONObject()
          .put(ID, newId)
          .put(SYNC_STATE, SyncState.NOT_SYNCED)
          .put(TIMEOUT, timeout)
          .put(NAME, name));
      call.setParamsJson(paramsJson);
      return null;
    }).continueWith(_task -> {
      if (_task.isFaulted()) {
        task.setError(_task.getError());
      } else {
        final RealmObjectObserver<MethodCall> observer =
            realmHelper.createObjectObserver(realm ->
                realm.where(MethodCall.class).equalTo(ID, newId));
        observer.setOnUpdateListener(methodCall -> {
          if (methodCall == null) {
            observer.unsub();
            REF_MAP.remove(newId);
            return;
          }

          int syncState = methodCall.getSyncState();
          RCLog.d("MethodCall[%s] syncstate=%d", methodCall.getMethodCallId(), syncState);
          if (syncState == SyncState.SYNCED) {
            String resultJson = methodCall.getResultJson();
            if (TextUtils.isEmpty(resultJson)) {
              task.setResult(null);
            } else {
              task.setResult(resultJson);
            }
            observer.unsub();
            REF_MAP.remove(methodCall.getMethodCallId());
            remove(realmHelper, methodCall.getMethodCallId()).continueWith(new LogcatIfError());
          } else if (syncState == SyncState.FAILED) {
            task.setError(new Error(methodCall.getResultJson()));
            observer.unsub();
            REF_MAP.remove(methodCall.getMethodCallId());
            remove(realmHelper, methodCall.getMethodCallId()).continueWith(new LogcatIfError());
          }
        });
        observer.sub();
        REF_MAP.put(newId, observer);
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
            .equalTo(ID, methodCallId)
            .findAll()
            .deleteAllFromRealm());
  }

  public String getMethodCallId() {
    return methodCallId;
  }

  public void setMethodCallId(String methodCallId) {
    this.methodCallId = methodCallId;
  }

  public int getSyncState() {
    return syncstate;
  }

  public void setSyncState(int syncstate) {
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
}
