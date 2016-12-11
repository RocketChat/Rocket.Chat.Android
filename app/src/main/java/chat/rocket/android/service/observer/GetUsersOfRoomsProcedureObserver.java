package chat.rocket.android.service.observer;

import android.content.Context;
import bolts.Task;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.internal.GetUsersOfRoomsProcedure;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import org.json.JSONObject;
import timber.log.Timber;

/**
 * Model observer for executing getUsersOfRooms.
 */
public class GetUsersOfRoomsProcedureObserver
    extends AbstractModelObserver<GetUsersOfRoomsProcedure> {

  private final MethodCallHelper methodCall;

  public GetUsersOfRoomsProcedureObserver(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    methodCall = new MethodCallHelper(realmHelper, ddpClient);
  }

  @Override public RealmResults<GetUsersOfRoomsProcedure> queryItems(Realm realm) {
    return realm.where(GetUsersOfRoomsProcedure.class)
        .equalTo("syncstate", SyncState.NOT_SYNCED)
        .findAll();
  }

  @Override public void onUpdateResults(List<GetUsersOfRoomsProcedure> results) {
    if (results == null || results.isEmpty()) {
      return;
    }

    GetUsersOfRoomsProcedure procedure = results.get(0);
    final String roomId = procedure.getRoomId();
    final boolean showAll = procedure.isShowAll();

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(GetUsersOfRoomsProcedure.class, new JSONObject()
            .put("roomId", roomId)
            .put("syncstate", SyncState.SYNCING))
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
        Timber.w(task.getError());
        return realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(GetUsersOfRoomsProcedure.class, new JSONObject()
                .put("roomId", roomId)
                .put("syncstate", SyncState.FAILED)));
      } else {
        return Task.forResult(null);
      }
    });
  }
}
