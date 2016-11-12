package chat.rocket.android.service.observer;

import android.content.Context;
import bolts.Task;
import chat.rocket.android.helper.MethodCallHelper;
import chat.rocket.android.model.LoadMessageProcedure;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.ws.RocketChatWebSocketAPI;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;
import org.json.JSONObject;
import timber.log.Timber;

/**
 * Background process for loading messages.
 */
public class LoadMessageProcedureObserver extends AbstractModelObserver<LoadMessageProcedure> {

  private final MethodCallHelper methodCall;

  public LoadMessageProcedureObserver(Context context, String serverConfigId,
      RocketChatWebSocketAPI api) {
    super(context, serverConfigId, api);
    methodCall = new MethodCallHelper(serverConfigId, api);
  }

  @Override protected RealmResults<LoadMessageProcedure> queryItems(Realm realm) {
    return realm.where(LoadMessageProcedure.class)
        .equalTo("serverConfigId", serverConfigId)
        .equalTo("syncstate", SyncState.NOT_SYNCED)
        .findAll();
  }

  @Override protected void onCollectionChanged(List<LoadMessageProcedure> list) {
    if (list == null || list.isEmpty()) {
      return;
    }

    LoadMessageProcedure procedure = list.get(0);
    final String roomId = procedure.getRoomId();
    final boolean isReset = procedure.isReset();
    final long timestamp = procedure.getTimestamp();
    final int count = procedure.getCount();
    final long lastSeen = 0; // TODO: Not implemented yet.

    RealmHelperBolts.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
            .put("roomId", roomId)
            .put("syncstate", SyncState.SYNCING))
    ).onSuccessTask(task ->
        methodCall.loadHistory(roomId, isReset ? 0 : timestamp, count, lastSeen)
            .onSuccessTask(_task ->
                RealmHelperBolts.executeTransaction(realm ->
                    realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
                        .put("roomId", roomId)
                        .put("syncstate", SyncState.SYNCED))))
    ).continueWithTask(task -> {
      if (task.isFaulted()) {
        Timber.w(task.getError());
        return RealmHelperBolts.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
                .put("roomId", roomId)
                .put("syncstate", SyncState.FAILED)));
      } else {
        return Task.forResult(null);
      }
    });
  }
}
