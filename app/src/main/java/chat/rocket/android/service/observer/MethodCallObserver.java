package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.MethodCall;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import chat.rocket.android_ddp.DDPClientCallback;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;

/**
 * Observing MethodCall record, executing RPC if needed.
 */
public class MethodCallObserver extends AbstractModelObserver<MethodCall> {

  /**
   * constructor.
   */
  public MethodCallObserver(Context context, String serverConfigId, RocketChatWebSocketAPI api) {
    super(context, serverConfigId, api);
    RealmHelperBolts.executeTransaction(realm -> {
      RealmResults<MethodCall> pendingMethodCalls = realm.where(MethodCall.class)
          .equalTo("serverConfigId", serverConfigId)
          .equalTo("syncstate", SyncState.SYNCING)
          .findAll();
      for (MethodCall call : pendingMethodCalls) {
        call.setSyncstate(SyncState.NOT_SYNCED);
      }

      // clean up records.
      realm.where(MethodCall.class)
          .equalTo("serverConfigId", serverConfigId)
          .beginGroup()
          .equalTo("syncstate", SyncState.SYNCED)
          .or()
          .equalTo("syncstate", SyncState.FAILED)
          .endGroup()
          .findAll().deleteAllFromRealm();
      return null;
    }).continueWith(new LogcatIfError());
  }

  @Override protected RealmResults<MethodCall> queryItems(Realm realm) {
    return realm.where(MethodCall.class)
        .isNotNull("name")
        .equalTo("serverConfigId", serverConfigId)
        .equalTo("syncstate", SyncState.NOT_SYNCED)
        .findAll();
  }

  @Override protected void onCollectionChanged(List<MethodCall> list) {
    if (list == null || list.isEmpty()) {
      return;
    }

    MethodCall call = list.get(0);
    final String methodCallId = call.getMethodCallId();
    final String methodName = call.getName();
    final String params = call.getParamsJson();
    final long timeout = call.getTimeout();
    RealmHelperBolts.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
          .put("methodCallId", methodCallId)
          .put("syncstate", SyncState.SYNCING))
    ).onSuccessTask(task ->
        webSocketAPI.rpc(methodCallId, methodName, params, timeout)
            .onSuccessTask(_task -> RealmHelperBolts.executeTransaction(realm -> {
              String json = _task.getResult().result;
              return realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
                  .put("methodCallId", methodCallId)
                  .put("syncstate", SyncState.SYNCED)
                  .put("resultJson", json));
            })
        )
    ).continueWithTask(task -> {
      if (task.isFaulted()) {
        return RealmHelperBolts.executeTransaction(realm -> {
          Exception exception = task.getError();
          final String errMessage = (exception instanceof DDPClientCallback.RPC.Error)
              ? ((DDPClientCallback.RPC.Error) exception).error.toString()
              : exception.getMessage();
          realm.createOrUpdateObjectFromJson(MethodCall.class, new JSONObject()
              .put("methodCallId", methodCallId)
              .put("syncstate", SyncState.FAILED)
              .put("resultJson", errMessage));
          return null;
        });
      }
      return task;
    }).continueWith(new LogcatIfError());
  }
}
