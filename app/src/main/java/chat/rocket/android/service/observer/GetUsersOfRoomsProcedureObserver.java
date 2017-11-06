package chat.rocket.android.service.observer;

import android.content.Context;

import org.json.JSONObject;

import java.util.List;

import bolts.Task;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.internal.GetUsersOfRoomsProcedure;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Model observer for executing getUsersOfRooms.
 */
public class GetUsersOfRoomsProcedureObserver
    extends AbstractModelObserver<GetUsersOfRoomsProcedure> {

  private final MethodCallHelper methodCall;

  public GetUsersOfRoomsProcedureObserver(Context context, String hostname,
                                          RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
    methodCall = new MethodCallHelper(realmHelper);
  }

  @Override
  public RealmResults<GetUsersOfRoomsProcedure> queryItems(Realm realm) {
    return realm.where(GetUsersOfRoomsProcedure.class)
        .equalTo(GetUsersOfRoomsProcedure.SYNC_STATE, SyncState.NOT_SYNCED)
        .findAll();
  }

  @Override
  public void onUpdateResults(List<GetUsersOfRoomsProcedure> results) {
    if (results == null || results.isEmpty()) {
      return;
    }

    GetUsersOfRoomsProcedure procedure = results.get(0);
    final String roomId = procedure.getRoomId();
    final boolean showAll = procedure.isShowAll();

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(GetUsersOfRoomsProcedure.class, new JSONObject()
            .put(GetUsersOfRoomsProcedure.ID, roomId)
            .put(GetUsersOfRoomsProcedure.SYNC_STATE, SyncState.SYNCING))
    ).onSuccessTask(task ->
        methodCall.getUsersOfRoom(roomId, showAll)
            .onSuccessTask(_task -> {
              JSONObject result = _task.getResult()
                  .put("roomId", roomId)
                  .put("syncstate", SyncState.SYNCED);

              return realmHelper.executeTransaction(realm ->
                  realm.createOrUpdateObjectFromJson(GetUsersOfRoomsProcedure.class, result));
            })
    ).continueWithTask(task -> {
      if (task.isFaulted()) {
        RCLog.w(task.getError());
        return realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(GetUsersOfRoomsProcedure.class, new JSONObject()
                .put(GetUsersOfRoomsProcedure.ID, roomId)
                .put(GetUsersOfRoomsProcedure.SYNC_STATE, SyncState.FAILED)));
      } else {
        return Task.forResult(null);
      }
    });
  }
}
