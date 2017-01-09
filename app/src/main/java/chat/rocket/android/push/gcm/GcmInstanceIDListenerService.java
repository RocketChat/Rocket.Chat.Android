package chat.rocket.android.push.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import android.content.Intent;

import java.util.List;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.ddp.PublicSettingsConstants;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;

public class GcmInstanceIDListenerService extends InstanceIDListenerService {

  @Override
  public void onTokenRefresh() {
    updateSyncPushTokenIfNeeded();

    if (!shouldRefreshToken()) {
      return;
    }

    Intent intent = new Intent(this, GcmRegistrationIntentService.class);
    startService(intent);
  }

  private void updateSyncPushTokenIfNeeded() {
    final RealmHelper realmHelper = RealmStore.getDefault();
    List<ServerConfig> serverConfigs = realmHelper.executeTransactionForReadResults(
        realm -> realm.where(ServerConfig.class).findAll());

    for (final ServerConfig serverConfig : serverConfigs) {
      final RealmHelper serverRealmHelper = RealmStore.get(serverConfig.getServerConfigId());
      if (serverRealmHelper == null) {
        continue;
      }

      boolean isPushEnable = PublicSetting
          .getBoolean(serverRealmHelper, PublicSettingsConstants.Push.ENABLE, false);
      String senderId = PublicSetting
          .getString(serverRealmHelper, PublicSettingsConstants.Push.GCM_PROJECT_NUMBER, "").trim();

      boolean syncPushToken = isPushEnable && !"".equals(senderId);

      if (serverConfig.shouldSyncPushToken() != syncPushToken) {
        serverConfig.setSyncPushToken(syncPushToken);
        realmHelper.executeTransaction(realm -> realm.copyToRealmOrUpdate(serverConfig));
      }
    }
  }

  private boolean shouldRefreshToken() {
    return RealmStore.getDefault().isObjectExists(realm ->
        realm.where(ServerConfig.class).equalTo(ServerConfig.SYNC_PUSH_TOKEN, true));
  }
}