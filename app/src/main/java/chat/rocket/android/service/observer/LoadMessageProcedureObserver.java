package chat.rocket.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import org.json.JSONObject;

import java.util.List;
import bolts.Task;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.android.realm_helper.RealmHelper;

/**
 * Background process for loading messages.
 */
public class LoadMessageProcedureObserver extends AbstractModelObserver<LoadMessageProcedure> {

  private final MethodCallHelper methodCall;

  public LoadMessageProcedureObserver(Context context, String hostname,
                                      RealmHelper realmHelper, DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    methodCall = new MethodCallHelper(realmHelper, ddpClient);
  }

  @Override
  public RealmResults<LoadMessageProcedure> queryItems(Realm realm) {
    return realm.where(LoadMessageProcedure.class)
        .equalTo(LoadMessageProcedure.SYNC_STATE, SyncState.NOT_SYNCED)
        .findAll();
  }

  @Override
  public void onUpdateResults(List<LoadMessageProcedure> results) {
    if (results == null || results.isEmpty()) {
      return;
    }

    LoadMessageProcedure procedure = results.get(0);
    final String roomId = procedure.getRoomId();
    final boolean isReset = procedure.isReset();
    final long timestamp = procedure.getTimestamp();
    final int count = procedure.getCount();
    final long lastSeen = 0; // TODO: Not implemented yet.

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
            .put(LoadMessageProcedure.ID, roomId)
            .put(LoadMessageProcedure.SYNC_STATE, SyncState.SYNCING))
    ).onSuccessTask(task ->
        methodCall.loadHistory(roomId, isReset ? 0 : timestamp, count, lastSeen)
            .onSuccessTask(_task -> {
              Message lastMessage = realmHelper.executeTransactionForRead(realm ->
                  realm.where(Message.class)
                      .equalTo(Message.ROOM_ID, roomId)
                      .equalTo(Message.SYNC_STATE, SyncState.SYNCED)
                      .findAllSorted(Message.TIMESTAMP, Sort.ASCENDING).first(null));
              long lastTs = lastMessage != null ? lastMessage.getTimestamp() : 0;
              int messageCount = _task.getResult().length();
              return realmHelper.executeTransaction(realm ->
                  realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
                      .put(LoadMessageProcedure.ID, roomId)
                      .put(LoadMessageProcedure.SYNC_STATE, SyncState.SYNCED)
                      .put(LoadMessageProcedure.TIMESTAMP, lastTs)
                      .put(LoadMessageProcedure.RESET, false)
                      .put(LoadMessageProcedure.HAS_NEXT, messageCount == count)));
            })
    ).continueWithTask(task -> {
      if (task.isFaulted()) {
        RCLog.w(task.getError());
        return realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
                .put(LoadMessageProcedure.ID, roomId)
                .put(LoadMessageProcedure.SYNC_STATE, SyncState.FAILED)));
      } else {
        return Task.forResult(null);
      }
    });
  }
}
