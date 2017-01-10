package chat.rocket.android.push.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import java.util.List;
import chat.rocket.android.helper.GcmPushSettingHelper;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.internal.GcmPushRegistration;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;

public class GcmInstanceIDListenerService extends InstanceIDListenerService {

  @Override
  public void onTokenRefresh() {
    List<ServerConfig> serverConfigs = RealmStore.getDefault()
        .executeTransactionForReadResults(realm ->
            realm.where(ServerConfig.class)
                .isNotNull(ServerConfig.ID)
                .isNotNull(ServerConfig.HOSTNAME)
                .findAll());
    for (ServerConfig serverConfig : serverConfigs) {
      RealmHelper realmHelper = RealmStore.get(serverConfig.getServerConfigId());
      if (realmHelper != null) {
        updateGcmToken(realmHelper);
      }
    }
  }

  private void updateGcmToken(RealmHelper realmHelper) {
    final List<PublicSetting> results = realmHelper.executeTransactionForReadResults(
        GcmPushSettingHelper::queryForGcmPushEnabled);
    final boolean gcmPushEnabled = GcmPushSettingHelper.isGcmPushEnabled(results);

    if (gcmPushEnabled) {
      realmHelper.executeTransaction(realm ->
          GcmPushRegistration.updateGcmPushEnabled(realm, gcmPushEnabled));
    }
  }
}