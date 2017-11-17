package chat.rocket.android.push.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import java.util.List;

import chat.rocket.android.helper.GcmPushSettingHelper;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmPublicSetting;
import chat.rocket.persistence.realm.models.internal.GcmPushRegistration;

public class GcmInstanceIDListenerService extends InstanceIDListenerService {

  @Override
  public void onTokenRefresh() {
    List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(getApplicationContext())
        .getServerList();
    for (ServerInfo serverInfo : serverInfoList) {
      RealmHelper realmHelper = RealmStore.get(serverInfo.getHostname());
      if (realmHelper != null) {
        updateGcmToken(realmHelper);
      }
    }
  }

  private void updateGcmToken(RealmHelper realmHelper) {
    final List<RealmPublicSetting> results = realmHelper.executeTransactionForReadResults(
        GcmPushSettingHelper::queryForGcmPushEnabled);
    final boolean gcmPushEnabled = GcmPushSettingHelper.isGcmPushEnabled(results);

    if (gcmPushEnabled) {
      realmHelper.executeTransaction(realm ->
          GcmPushRegistration.updateGcmPushEnabled(realm, gcmPushEnabled));
    }
  }
}