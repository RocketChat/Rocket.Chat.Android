package chat.rocket.android.service.observer;

import android.content.Context;

import org.json.JSONObject;

import java.util.List;

import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.android.log.RCLog;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmMessage;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Observe messages for deletion.
 */
public class DeletedMessageObserver  extends AbstractModelObserver<RealmMessage> {

    private final MethodCallHelper methodCall;

    public DeletedMessageObserver(Context context, String hostname, RealmHelper realmHelper) {
        super(context, hostname, realmHelper);
        methodCall = new MethodCallHelper(realmHelper);

        realmHelper.executeTransaction(realm -> {
            // resume pending operations.
            RealmResults<RealmMessage> pendingMethodCalls = realm.where(RealmMessage.class)
                    .equalTo(RealmMessage.SYNC_STATE, SyncState.DELETING)
                    .findAll();
            for (RealmMessage message : pendingMethodCalls) {
                message.setSyncState(SyncState.DELETE_NOT_SYNCED);
            }

            return null;
        }).continueWith(new LogIfError());
    }

    @Override
    public RealmResults<RealmMessage> queryItems(Realm realm) {
        return realm.where(RealmMessage.class)
                .equalTo(RealmMessage.SYNC_STATE, SyncState.DELETE_NOT_SYNCED)
                .isNotNull(RealmMessage.ROOM_ID)
                .findAll();
    }

    @Override
    public void onUpdateResults(List<RealmMessage> results) {
        if (results.isEmpty()) {
            return;
        }

        for(RealmMessage message : results) {
            final String messageId = message.getId();

            realmHelper.executeTransaction(realm ->
                realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
                    .put(RealmMessage.ID, messageId)
                    .put(RealmMessage.SYNC_STATE, SyncState.DELETING)
                )
            ).onSuccessTask(task -> methodCall.deleteMessage(messageId)
            ).continueWith(task -> {
                if(task.isFaulted()) {
                    RCLog.w(task.getError());
                    realmHelper.executeTransaction(realm ->
                        realm.createOrUpdateObjectFromJson(RealmMessage.class, new JSONObject()
                            .put(RealmMessage.ID, messageId)
                            .put(RealmMessage.SYNC_STATE, SyncState.DELETE_FAILED)));
                } else {
                    realmHelper.executeTransaction(realm ->
                        realm.where(RealmMessage.class)
                                .equalTo(RealmMessage.ID, messageId)
                            .findAll()
                            .deleteAllFromRealm()
                    );
                }
                return null;
            });
        }
    }
}
