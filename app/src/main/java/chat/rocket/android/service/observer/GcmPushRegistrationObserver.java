package chat.rocket.android.service.observer;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.io.IOException;
import java.util.List;
import bolts.Task;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.RaixPushHelper;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.SyncState;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.ddp.PublicSettingsConstants;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.model.internal.GcmPushRegistration;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.service.DDPClientRef;

/**
 * call raix:push-update if needed.
 */
public class GcmPushRegistrationObserver extends AbstractModelObserver<GcmPushRegistration> {
  public GcmPushRegistrationObserver(Context context, String hostname,
                                        RealmHelper realmHelper,
                                        DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
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
        }).continueWith(new LogcatIfError());
      }
      return null;
    });
  }

  private Task<Void> registerGcmTokenForServer() throws IOException {
    final String senderId = PublicSetting
        .getString(realmHelper, PublicSettingsConstants.Push.GCM_PROJECT_NUMBER, "").trim();

    final String gcmToken = getGcmToken(senderId);
    final User currentUser = realmHelper.executeTransactionForRead(realm ->
        User.queryCurrentUser(realm).findFirst());
    final String userId = currentUser != null ? currentUser.getId() : null;
    final String pushId = RocketChatCache.getOrCreatePushId(context);

    return new RaixPushHelper(realmHelper, ddpClientRef)
        .pushUpdate(pushId, gcmToken, userId);
  }

  private String getGcmToken(String senderId) throws IOException {
    return InstanceID.getInstance(context)
        .getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
  }

}
