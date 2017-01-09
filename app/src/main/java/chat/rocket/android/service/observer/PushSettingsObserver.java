package chat.rocket.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.rocket.android.api.DDPClientWrapper;
import chat.rocket.android.helper.GcmPushSettingHelper;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.internal.GcmPushRegistration;
import chat.rocket.android.realm_helper.RealmHelper;

public class PushSettingsObserver extends AbstractModelObserver<PublicSetting> {

  public PushSettingsObserver(Context context, String hostname,
                              RealmHelper realmHelper, DDPClientWrapper ddpClient) {
    super(context, hostname, realmHelper, ddpClient);
  }

  @Override
  public RealmResults<PublicSetting> queryItems(Realm realm) {
    return GcmPushSettingHelper.queryForGcmPushEnabled(realm);
  }

  @Override
  public void onUpdateResults(List<PublicSetting> results) {
    boolean gcmPushEnabled = GcmPushSettingHelper.isGcmPushEnabled(results);

    GcmPushRegistration gcmPushRegistration = realmHelper.executeTransactionForRead(realm ->
        GcmPushRegistration.queryDefault(realm).findFirst());

    if (gcmPushRegistration == null || gcmPushEnabled != gcmPushRegistration.isGcmPushEnabled()) {
      realmHelper.executeTransaction(realm ->
          GcmPushRegistration.updateGcmPushEnabled(realm, gcmPushEnabled));
    }
  }
}
