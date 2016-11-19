package chat.rocket.android.service.observer;

import android.content.Context;
import bolts.Task;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.model.internal.LoadMessageProcedure;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.api.DDPClientWraper;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import java.util.List;
import org.json.JSONObject;
import timber.log.Timber;

/**
 * Background process for loading messages.
 */
public class LoadMessageProcedureObserver extends AbstractModelObserver<LoadMessageProcedure> {

  private final MethodCallHelper methodCall;

  public LoadMessageProcedureObserver(Context context, RealmHelper realmHelper,
      DDPClientWraper ddpClient) {
    super(context, realmHelper, ddpClient);
    methodCall = new MethodCallHelper(realmHelper, ddpClient);
  }

  @Override public RealmResults<LoadMessageProcedure> queryItems(Realm realm) {
    return realm.where(LoadMessageProcedure.class)
        .equalTo("syncstate", SyncState.NOT_SYNCED)
        .findAll();
  }

  @Override public void onUpdateResults(List<LoadMessageProcedure> results) {
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
            .put("roomId", roomId)
            .put("syncstate", SyncState.SYNCING))
    ).onSuccessTask(task ->
        methodCall.loadHistory(roomId, isReset ? 0 : timestamp, count, lastSeen)
            .onSuccessTask(_task -> {
              Message lastMessage = realmHelper.executeTransactionForRead(realm ->
                  realm.where(Message.class)
                      .equalTo("rid", roomId)
                      .equalTo("syncstate", SyncState.SYNCED)
                      .findAllSorted("ts", Sort.ASCENDING).last(null));
              long lastTs = lastMessage != null ? lastMessage.getTs() : 0;
              return realmHelper.executeTransaction(realm ->
                  realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
                      .put("roomId", roomId)
                      .put("syncstate", SyncState.SYNCED)
                      .put("timestamp", lastTs)
                      .put("hasNext", lastTs > 0))
              );
            })
    ).continueWithTask(task -> {
      if (task.isFaulted()) {
        Timber.w(task.getError());
        return realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(LoadMessageProcedure.class, new JSONObject()
                .put("roomId", roomId)
                .put("syncstate", SyncState.FAILED)));
      } else {
        return Task.forResult(null);
      }
    });
  }
}
