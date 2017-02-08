package chat.rocket.android.push.gcm;

import com.google.android.gms.iid.InstanceIDListenerService;

import java.util.List;
import chat.rocket.android.helper.GcmPushSettingHelper;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.internal.GcmPushRegistration;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.service.ServerInfo;

public class GcmInstanceIDListenerService extends InstanceIDListenerService {

  @Override
  public void onTokenRefresh() {
    List<ServerInfo> serverInfoList = ConnectivityManager.getInstance(getApplicationContext())
        .getServerList();
    for (ServerInfo serverInfo : serverInfoList) {
      RealmHelper realmHelper = RealmStore.get(serverInfo.hostname);
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