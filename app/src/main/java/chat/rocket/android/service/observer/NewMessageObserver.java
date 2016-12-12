package chat.rocket.android.service.observer;

import android.content.Context;
import chat.rocket.android.api.DDPClientWraper;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.log.RCLog;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.Message;
import chat.rocket.android.realm_helper.RealmHelper;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import org.json.JSONObject;

/**
 * Observe messages for sending.
 */
public class NewMessageObserver extends AbstractModelObserver<Message> {

  private final MethodCallHelper methodCall;

  public NewMessageObserver(Context context, String hostname,
      RealmHelper realmHelper, DDPClientWraper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
    methodCall = new MethodCallHelper(realmHelper, ddpClient);

    realmHelper.executeTransaction(realm -> {
      // resume pending operations.
      RealmResults<Message> pendingMethodCalls = realm.where(Message.class)
          .equalTo("syncstate", SyncState.SYNCING)
          .findAll();
      for (Message message : pendingMethodCalls) {
        message.setSyncstate(SyncState.NOT_SYNCED);
      }

      return null;
    }).continueWith(new LogcatIfError());
  }

  @Override public RealmResults<Message> queryItems(Realm realm) {
    return realm.where(Message.class)
        .equalTo("syncstate", SyncState.NOT_SYNCED)
        .isNotNull("rid")
        .findAll();
  }

  @Override public void onUpdateResults(List<Message> results) {
    if (results.isEmpty()) {
      return;
    }

    Message message = results.get(0);
    final String messageId = message.get_id();
    final String roomId = message.getRid();
    final String msg = message.getMsg();

    realmHelper.executeTransaction(realm ->
        realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
            .put("_id", messageId)
            .put("syncstate", SyncState.SYNCING)
        )
    ).onSuccessTask(task ->
        methodCall.sendMessage(messageId, roomId, msg).onSuccessTask(_task -> {
          JSONObject messageJson = _task.getResult();
          messageJson.put("syncstate", SyncState.SYNCED);
          return realmHelper.executeTransaction(realm ->
              realm.createOrUpdateObjectFromJson(Message.class, messageJson));
        })
    ).continueWith(task -> {
      if (task.isFaulted()) {
        RCLog.w(task.getError());
        realmHelper.executeTransaction(realm ->
            realm.createOrUpdateObjectFromJson(Message.class, new JSONObject()
                .put("_id", messageId)
                .put("syncstate", SyncState.FAILED)));
      }
      return null;
    });
  }
}
