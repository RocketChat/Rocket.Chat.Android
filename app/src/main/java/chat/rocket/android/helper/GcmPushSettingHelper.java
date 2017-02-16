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
        .or()
        .equalTo(RealmPublicSetting.ID, PublicSettingsConstants.Push.GCM_PROJECT_NUMBER)
        .findAll();
  }

  public static boolean isGcmPushEnabled(List<RealmPublicSetting> results) {
    return isPushEnabled(results) && hasValidGcmConfig(results);
  }

  private static boolean isPushEnabled(List<RealmPublicSetting> results) {
    for (RealmPublicSetting setting : results) {
      if (PublicSettingsConstants.Push.ENABLE.equals(setting.getId())) {
        return "true".equals(setting.getValue());
      }
    }
    return false;
  }

  private static boolean hasValidGcmConfig(List<RealmPublicSetting> results) {
    for (RealmPublicSetting setting : results) {
      if (PublicSettingsConstants.Push.GCM_PROJECT_NUMBER.equals(setting.getId())) {
        return !TextUtils.isEmpty(setting.getValue());
      }
    }
    return false;
  }
}
