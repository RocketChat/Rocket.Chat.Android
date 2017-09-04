package chat.rocket.android.helper;

import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.rocket.persistence.realm.models.ddp.RealmPublicSetting;
import chat.rocket.core.PublicSettingsConstants;

/**
 * utility class for getting value comprehensibly from public settings list.
 */
public class GcmPushSettingHelper {

  public static RealmResults<RealmPublicSetting> queryForGcmPushEnabled(Realm realm) {
    return realm.where(RealmPublicSetting.class)
        .equalTo(RealmPublicSetting.ID, PublicSettingsConstants.Push.ENABLE)
        .findAll();
  }

  public static boolean isGcmPushEnabled(List<RealmPublicSetting> results) {
    return isPushEnabled(results);
  }

  private static boolean isPushEnabled(List<RealmPublicSetting> results) {
    for (RealmPublicSetting setting : results) {
      if (PublicSettingsConstants.Push.ENABLE.equals(setting.getId())) {
        return "true".equals(setting.getValue());
      }
    }
    return false;
  }
}
