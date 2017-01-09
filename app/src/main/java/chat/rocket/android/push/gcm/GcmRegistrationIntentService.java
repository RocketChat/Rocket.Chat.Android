package chat.rocket.android.push.gcm;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import android.app.IntentService;
import android.content.Intent;

import java.io.IOException;
import java.util.List;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.PushHelper;
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
    final List<ServerConfig> serverConfigs = getServerConfigs();
    for (ServerConfig serverConfig : serverConfigs) {
      sendTokenTo(serverConfig);
    }
  }

  private List<ServerConfig> getServerConfigs() {
    return RealmStore.getDefault().executeTransactionForReadResults(
        realm -> realm.where(ServerConfig.class).findAll());
  }

  private void sendTokenTo(final ServerConfig serverConfig) {
    if (!serverConfig.shouldSyncPushToken()) {
      return;
    }

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
      final String token = getToken(senderId);

      final User currentUser = realmHelper.executeTransactionForRead(realm ->
          User.queryCurrentUser(realm).findFirst());

      new PushHelper(getBaseContext(), serverConfig.getServerConfigId()).pushUpdate(
          RocketChatCache.getPushId(this), token, currentUser != null ? currentUser.getId() : null)
          .onSuccess(task -> {
            markRefreshAsDone(serverConfig);
            return task;
          });
    } catch (Exception e) {
    }
  }

  private String getToken(String senderId) throws IOException {
    return InstanceID.getInstance(this)
        .getToken(senderId, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
  }

  private void markRefreshAsDone(ServerConfig serverConfig) {
    serverConfig.setSyncPushToken(false);
    RealmStore.getDefault().executeTransaction(realm -> realm.copyToRealm(serverConfig));
  }
}