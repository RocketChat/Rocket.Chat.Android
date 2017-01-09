package chat.rocket.android.push.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.IntentService;
import android.content.Intent;

import java.io.IOException;
import java.util.List;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.RaixPushHelper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.ddp.PublicSettingsConstants;
import chat.rocket.android.model.ddp.User;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;

public class GcmRegistrationIntentService extends IntentService {

  public GcmRegistrationIntentService() {
    super("GcmRegistrationIntentService");
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    final List<ServerConfig> serverConfigs = RealmStore.getDefault()
        .executeTransactionForReadResults(realm ->
            realm.where(ServerConfig.class).equalTo(ServerConfig.SYNC_PUSH_TOKEN, true).findAll());
    for (ServerConfig serverConfig : serverConfigs) {
      registerGcmTokenForServer(serverConfig);
    }
  }

  private void registerGcmTokenForServer(final ServerConfig serverConfig) {
    final RealmHelper realmHelper = RealmStore.get(serverConfig.getServerConfigId());
    if (realmHelper == null) {
      return;
    }

    final String senderId = PublicSetting
        .getString(realmHelper, PublicSettingsConstants.Push.GCM_PROJECT_NUMBER, "").trim();
    if ("".equals(senderId)) {
      markRefreshAsDone(serverConfig);
      return;
    }

    try {
      final String gcmToken = getGcmToken(senderId);

      final User currentUser = realmHelper.executeTransactionForRead(realm ->
          User.queryCurrentUser(realm).findFirst());

      final String pushId = RocketChatCache.getOrCreatePushId(this);
      final String userId = currentUser != null ? currentUser.getId() : null;
      new RaixPushHelper(getBaseContext(), serverConfig.getServerConfigId())
          .pushUpdate(pushId, gcmToken, userId)
          .onSuccess(task -> {
            markRefreshAsDone(serverConfig);
            return task;
          });
    } catch (Exception e) {
    }
  }

  private String getGcmToken(String senderId) throws IOException {
    return InstanceID.getInstance(this)
        .getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
  }

  private void markRefreshAsDone(ServerConfig serverConfig) {
    serverConfig.setSyncPushToken(false);
    RealmStore.getDefault().executeTransaction(realm -> realm.copyToRealm(serverConfig));
  }
}