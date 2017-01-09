package chat.rocket.android.push.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

import java.util.List;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.ddp.PublicSettingsConstants;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;

public class GcmInstanceIDListenerService extends InstanceIDListenerService {

  @Override
  public void onTokenRefresh() {
    List<ServerConfig> serverConfigs = getServerConfigs();

    updateSyncPushToken(serverConfigs);

    if (!shouldRefreshToken(serverConfigs)) {
      return;
    }

    Intent intent = new Intent(this, GcmRegistrationIntentService.class);
    startService(intent);
  }

  private List<ServerConfig> getServerConfigs() {
    return RealmStore.getDefault().executeTransactionForReadResults(
        realm -> realm.where(ServerConfig.class).findAll());
  }

  private void updateSyncPushToken(List<ServerConfig> serverConfigs) {
    final RealmHelper realmHelper = RealmStore.getDefault();

    for (final ServerConfig serverConfig : serverConfigs) {
      final RealmHelper serverRealmHelper = RealmStore
          .getOrCreate(serverConfig.getServerConfigId());

      boolean isPushEnable = PublicSetting
          .getBoolean(serverRealmHelper, PublicSettingsConstants.Push.ENABLE, false);
      String senderId = PublicSetting
          .getString(serverRealmHelper, PublicSettingsConstants.Push.GCM_PROJECT_NUMBER, "").trim();

      serverConfig.setSyncPushToken(isPushEnable && !"".equals(senderId));

      realmHelper.executeTransaction(realm -> realm.copyToRealmOrUpdate(serverConfig));
    }
  }

  private boolean shouldRefreshToken(List<ServerConfig> serverConfigs) {
    for (ServerConfig serverConfig : serverConfigs) {
      if (serverConfig.shouldSyncPushToken()) {
        return true;
      }
    }

    return false;
  }
}