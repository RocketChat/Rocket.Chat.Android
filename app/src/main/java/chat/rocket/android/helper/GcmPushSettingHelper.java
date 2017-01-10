package chat.rocket.android.helper;

import io.realm.Realm;
import io.realm.RealmResults;

import java.util.List;
import chat.rocket.android.model.ddp.PublicSetting;
import chat.rocket.android.model.ddp.PublicSettingsConstants;

/**
 * utility class for getting value comprehensibly from public settings list.
 */
public class GcmPushSettingHelper {

  public static RealmResults<PublicSetting> queryForGcmPushEnabled(Realm realm) {
    return realm.where(PublicSetting.class)
        .equalTo(PublicSetting.ID, PublicSettingsConstants.Push.ENABLE)
        .or()
        .equalTo(PublicSetting.ID, PublicSettingsConstants.Push.GCM_PROJECT_NUMBER)
        .findAll();
  }

  public static boolean isGcmPushEnabled(List<PublicSetting> results) {
    return isPushEnabled(results) && hasValidGcmConfig(results);
  }

  private static boolean isPushEnabled(List<PublicSetting> results) {
    for (PublicSetting setting : results) {
      if (PublicSettingsConstants.Push.ENABLE.equals(setting.getId())) {
        return "true".equals(setting.getValue());
      }
    }
    return false;
  }

  private static boolean hasValidGcmConfig(List<PublicSetting> results) {
    for (PublicSetting setting : results) {
      if (PublicSettingsConstants.Push.GCM_PROJECT_NUMBER.equals(setting.getId())) {
        return !TextUtils.isEmpty(setting.getValue());
      }
    }
    return false;
  }
}
