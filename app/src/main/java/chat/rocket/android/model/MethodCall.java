package chat.rocket.android.model;

import bolts.Task;
import bolts.TaskCompletionSource;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;
import java.util.UUID;
import jp.co.crowdworks.realm_java_helpers.RealmObjectObserver;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("PMD.ShortVariable")
public class MethodCall extends RealmObject {

  @PrimaryKey private String id;
  private int syncstate;
  private String name;
  private String paramsJson;
  private String resultJson;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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

  public static class Error extends Exception {
    public Error(String message) {
      super(message);
    }

    public Error(Throwable exception) {
      super(exception);
    }
  }

  public static Task<JSONObject> execute(String name, String paramsJson) {
    final String newId = UUID.randomUUID().toString();
    TaskCompletionSource<JSONObject> task = new TaskCompletionSource<>();
    RealmHelperBolts.executeTransaction(realm -> {
      MethodCall call = realm.createObject(MethodCall.class, newId);
      call.setSyncstate(SyncState.NOT_SYNCED);
      call.setName(name);
      call.setParamsJson(paramsJson);
      return null;
    }).continueWith(_task -> {
      if (_task.isFaulted()) {
        task.setError(_task.getError());
      } else {
        new RealmObjectObserver<MethodCall>() {
          @Override protected RealmQuery<MethodCall> query(Realm realm) {
            return realm.where(MethodCall.class).equalTo("id", newId);
          }

          @Override protected void onChange(MethodCall methodCall) {
            int syncstate = methodCall.getSyncstate();
            if (syncstate == SyncState.SYNCED) {
              try {
                String resultJson = methodCall.getResultJson();
                task.setResult(TextUtils.isEmpty(resultJson) ? null : new JSONObject(resultJson));
              } catch (JSONException exception) {
                task.setError(new Error(exception));
              }
              exit(methodCall.getId());
            } else if (syncstate == SyncState.FAILED) {
              task.setError(new Error(methodCall.getResultJson()));
              exit(methodCall.getId());
            }
          }

          private void exit(String newId) {
            unsub();
            remove(newId).continueWith(new LogcatIfError());
          }
        }.sub();
      }
      return null;
    });
    return task.getTask();
  }

  public static final Task<Void> remove(String id) {
    return RealmHelperBolts.executeTransaction(realm ->
        realm.where(MethodCall.class).equalTo("id", id).findAll().deleteAllFromRealm());
  }
}
