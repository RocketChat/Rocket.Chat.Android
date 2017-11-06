package chat.rocket.android.service.observer;

import android.content.Context;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
import java.util.List;

import bolts.Task;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.RaixPushHelper;
import chat.rocket.android.helper.LogIfError;
import chat.rocket.core.SyncState;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.models.ddp.RealmUser;
import chat.rocket.persistence.realm.models.internal.GcmPushRegistration;
import io.realm.Realm;
import io.realm.RealmResults;

/**
 * call raix:push-update if needed.
 */
public class GcmPushRegistrationObserver extends AbstractModelObserver<GcmPushRegistration> {
  public GcmPushRegistrationObserver(Context context, String hostname,
                                     RealmHelper realmHelper) {
    super(context, hostname, realmHelper);
  }

  @Override
  public RealmResults<GcmPushRegistration> queryItems(Realm realm) {
    return GcmPushRegistration.queryDefault(realm)
        .equalTo(GcmPushRegistration.SYNC_STATE, SyncState.NOT_SYNCED)
        .equalTo(GcmPushRegistration.GCM_PUSH_ENABLED, true)
        .findAll();
  }

  @Override
  public void onUpdateResults(List<GcmPushRegistration> results) {
    if (results.isEmpty()) {
      return;
    }

    realmHelper.executeTransaction(realm -> {
      GcmPushRegistration.queryDefault(realm).findFirst().setSyncState(SyncState.SYNCING);
      return null;
    }).onSuccessTask(_task -> registerGcmTokenForServer()
    ).onSuccessTask(_task ->
        realmHelper.executeTransaction(realm -> {
          GcmPushRegistration.queryDefault(realm).findFirst().setSyncState(SyncState.SYNCED);
          return null;
        })
    ).continueWith(task -> {
      if (task.isFaulted()) {
        realmHelper.executeTransaction(realm -> {
          GcmPushRegistration.queryDefault(realm).findFirst().setSyncState(SyncState.FAILED);
          return null;
        }).continueWith(new LogIfError());
      }
      return null;
    });
  }

  private Task<Void> registerGcmTokenForServer() throws IOException {
    final String gcmToken = getGcmToken(getSenderId());
    final RealmUser currentUser = realmHelper.executeTransactionForRead(realm ->
        RealmUser.queryCurrentUser(realm).findFirst());
    final String userId = currentUser != null ? currentUser.getId() : null;
    final String pushId = new RocketChatCache(context).getOrCreatePushId();

    return new RaixPushHelper(realmHelper)
        .pushUpdate(pushId, gcmToken, userId);
  }

  private String getGcmToken(String senderId) throws IOException {
    return InstanceID.getInstance(context)
        .getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
  }

  private String getSenderId() {
    return context.getString(R.string.gcm_sender_id);
  }

}
