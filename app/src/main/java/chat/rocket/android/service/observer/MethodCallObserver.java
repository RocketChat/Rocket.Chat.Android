package chat.rocket.android.service.observer;

import android.content.Context;

import org.json.JSONObject;

import java.util.List;

import chat.rocket.android.helper.CheckSum;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android_ddp.DDPClient;
import chat.rocket.android_ddp.DDPClientCallback;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.internal.MethodCall;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Observing MethodCall record, executing RPC if needed.
 */
public class MethodCallObserver extends AbstractModelObserver<MethodCall> {

  private String prevDigest;

  /**
   * constructor.
   */
  public MethodCallObserver(Context context, String hostname,
                            RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
    realmHelper.executeTransaction(realm -> {
      // resume pending operations.
      RealmResults<MethodCall> pendingMethodCalls = realm.where(MethodCall.class)
          .equalTo(MethodCall.SYNC_STATE, SyncState.SYNCING)
          .findAll();
      for (MethodCall call : pendingMethodCalls) {
        call.setSyncState(SyncState.NOT_SYNCED);
      }

      // clean up records.
      realm.where(MethodCall.class)
          .beginGroup()
          .equalTo(MethodCall.SYNC_STATE, SyncState.SYNCED)
          .or()
          .equalTo(MethodCall.SYNC_STATE, SyncState.FAILED)
          .endGroup()
          .findAll().deleteAllFromRealm();
      return null;
    }).continueWith(new LogIfError());
  }

  @Override
  public RealmResults<MethodCall> queryItems(Realm realm) {
    return realm.where(MethodCall.class)
        .isNotNull(MethodCall.NAME)
        .equalTo(MethodCall.SYNC_STATE, SyncState.NOT_SYNCED)
        .findAll();
  }

  private String getDigestFor(List<MethodCall> results) {
    if (results == null) {
      return "-";
    }
    if (results.isEmpty()) {
      return "[]";
    }
    StringBuilder stringBuilder = new StringBuilder();
    for (MethodCall result : results) {
      stringBuilder.append(result.getMethodCallId());
    }
    return CheckSum.sha256(stringBuilder.toString());
  }

  @Override
  public void onUpdateResults(List<MethodCall> results) {
    String digest = getDigestFor(results);
    if (prevDigest == null) {
      if (digest == null) {
        return;
      }
    } else {
      if (prevDigest.equals(digest)) {
        return;
      }
    }
    prevDigest = digest;
    if (results == null || results.isEmpty()) {
      return;
    }

    MethodCall call = results.get(0);
    final String methodCallId = call.getMethodCallId();
    final String methodName = call.getName();
    final String params = call.getParamsJson();
    final long timeout = call.getTimeout();
    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
            .put(MethodCall.ID, methodCallId)
            .put(MethodCall.SYNC_STATE, SyncState.SYNCING))
    ).onSuccessTask(task ->
            DDPClient.get().rpc(methodCallId, methodName, params, timeout)
            .onSuccessTask(_task -> realmHelper.executeTransaction(realm -> {
              String json = _task.getResult().result;
              return realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
                  .put(MethodCall.ID, methodCallId)
                  .put(MethodCall.SYNC_STATE, SyncState.SYNCED)
                  .put(MethodCall.RESULT_JSON, json));
            }))
    ).continueWithTask(task -> {
      if (task.isFaulted()) {
        return realmHelper.executeTransaction(realm -> {
          Exception exception = task.getError();
          final String errMessage;

          if (exception instanceof DDPClientCallback.RPC.Error) {
            errMessage = ((DDPClientCallback.RPC.Error) exception).error.toString();
          } else if (exception instanceof DDPClientCallback.RPC.Timeout) {
            // temp "fix"- we need to rewrite the connection layer a bit
            errMessage = "{\"message\": \"Connection Timeout\"}";
          } else {
            errMessage = exception.getMessage();
          }

          realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
              .put(MethodCall.ID, methodCallId)
              .put(MethodCall.SYNC_STATE, SyncState.FAILED)
              .put(MethodCall.RESULT_JSON, errMessage));
          return null;
        });
      }
      return task;
    }).continueWith(new LogIfError());
  }
}
