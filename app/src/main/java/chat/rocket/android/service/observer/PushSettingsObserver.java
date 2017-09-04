package chat.rocket.android.service.observer;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.rocket.android.helper.GcmPushSettingHelper;
import chat.rocket.persistence.realm.models.ddp.RealmPublicSetting;
import chat.rocket.persistence.realm.models.internal.GcmPushRegistration;
import chat.rocket.persistence.realm.RealmHelper;
import chat.rocket.android.service.DDPClientRef;

public class PushSettingsObserver extends AbstractModelObserver<RealmPublicSetting> {

  public PushSettingsObserver(Context context, String hostname,
                              RealmHelper realmHelper, DDPClientRef ddpClientRef) {
    super(context, hostname, realmHelper, ddpClientRef);
  }

  @Override
  public RealmResults<RealmPublicSetting> queryItems(Realm realm) {
    return GcmPushSettingHelper.queryForGcmPushEnabled(realm);
  }

  @Override
  public void onUpdateResults(List<RealmPublicSetting> results) {
    boolean gcmPushEnabled = GcmPushSettingHelper.isGcmPushEnabled(results);

    if (gcmPushEnabled) {
      realmHelper.executeTransaction(realm ->
          GcmPushRegistration.updateGcmPushEnabled(realm, gcmPushEnabled));
    }
  }
}
