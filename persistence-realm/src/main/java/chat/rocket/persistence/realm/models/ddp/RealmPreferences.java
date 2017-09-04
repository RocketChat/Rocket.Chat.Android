package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import chat.rocket.core.models.Preferences;

@SuppressWarnings({"PMD.ShortVariable"})
public class RealmPreferences extends RealmObject {

  @PrimaryKey private String id;

  private String newRoomNotification;
  private String newMessageNotification;
  private boolean useEmojis;
  private boolean convertAsciiEmoji;
  private boolean saveMobileBandwidth;
  private boolean collapseMediaByDefault;
  private boolean unreadRoomsMode;
  private boolean autoImageLoad;
  private String emailNotificationMode;
  private boolean unreadAlert;
  private int desktopNotificationDuration;
  private int viewMode;
  private boolean hideUsernames;
  private boolean hideAvatars;
  private boolean hideFlexTab;
//  private List<String> highlights; // Realm does not support this yet

  public Preferences asPreferences() {
    return Preferences.builder()
        .setId(id)
        .setNewRoomNotification(newRoomNotification)
        .setNewMessageNotification(newMessageNotification)
        .setUseEmojis(useEmojis)
        .setConvertAsciiEmoji(convertAsciiEmoji)
        .setSaveMobileBandwidth(saveMobileBandwidth)
        .setCollapseMediaByDefault(collapseMediaByDefault)
        .setUnreadRoomsMode(unreadRoomsMode)
        .setAutoImageLoad(autoImageLoad)
        .setEmailNotificationMode(emailNotificationMode)
        .setUnreadAlert(unreadAlert)
        .setDesktopNotificationDuration(desktopNotificationDuration)
        .setViewMode(viewMode)
        .setHideUsernames(hideUsernames)
        .setHideAvatars(hideAvatars)
        .setHideFlexTab(hideFlexTab)
        .build();
  }

  public String getNewRoomNotification() {
    return newRoomNotification;
  }

  public String getNewMessageNotification() {
    return newMessageNotification;
  }

  public boolean isUseEmojis() {
    return useEmojis;
  }

  public boolean isConvertAsciiEmoji() {
    return convertAsciiEmoji;
  }

  public boolean isSaveMobileBandwidth() {
    return saveMobileBandwidth;
  }

  public boolean isCollapseMediaByDefault() {
    return collapseMediaByDefault;
  }

  public boolean isUnreadRoomsMode() {
    return unreadRoomsMode;
  }

  public boolean isAutoImageLoad() {
    return autoImageLoad;
  }

  public String getEmailNotificationMode() {
    return emailNotificationMode;
  }

  public boolean isUnreadAlert() {
    return unreadAlert;
  }

  public int getDesktopNotificationDuration() {
    return desktopNotificationDuration;
  }

  public int getViewMode() {
    return viewMode;
  }

  public boolean isHideUsernames() {
    return hideUsernames;
  }

  public boolean isHideAvatars() {
    return hideAvatars;
  }

  public boolean isHideFlexTab() {
    return hideFlexTab;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RealmPreferences that = (RealmPreferences) o;

    if (newRoomNotification != null ? newRoomNotification.equals(that.newRoomNotification)
        : that.newRoomNotification == null) {
      return false;
    }
    if (newMessageNotification != null ? newMessageNotification.equals(that.newMessageNotification)
        : that.newMessageNotification == null) {
      return false;
    }
    if (useEmojis != that.useEmojis) {
      return false;
    }
    if (convertAsciiEmoji != that.convertAsciiEmoji) {
      return false;
    }
    if (saveMobileBandwidth != that.saveMobileBandwidth) {
      return false;
    }
    if (collapseMediaByDefault != that.collapseMediaByDefault) {
      return false;
    }
    if (unreadRoomsMode != that.unreadRoomsMode) {
      return false;
    }
    if (autoImageLoad != that.autoImageLoad) {
      return false;
    }
    if (unreadAlert != that.unreadAlert) {
      return false;
    }
    if (desktopNotificationDuration != that.desktopNotificationDuration) {
      return false;
    }
    if (viewMode != that.viewMode) {
      return false;
    }
    if (hideUsernames != that.hideUsernames) {
      return false;
    }
    if (hideAvatars != that.hideAvatars) {
      return false;
    }
    if (hideFlexTab != that.hideFlexTab) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    return emailNotificationMode != null ? emailNotificationMode.equals(that.emailNotificationMode)
        : that.emailNotificationMode == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (newRoomNotification != null ? newRoomNotification.hashCode() : 0);
    result = 31 * result + (newMessageNotification != null ? newMessageNotification.hashCode() : 0);
    result = 31 * result + (useEmojis ? 1 : 0);
    result = 31 * result + (convertAsciiEmoji ? 1 : 0);
    result = 31 * result + (saveMobileBandwidth ? 1 : 0);
    result = 31 * result + (collapseMediaByDefault ? 1 : 0);
    result = 31 * result + (unreadRoomsMode ? 1 : 0);
    result = 31 * result + (autoImageLoad ? 1 : 0);
    result = 31 * result + (emailNotificationMode != null ? emailNotificationMode.hashCode() : 0);
    result = 31 * result + (unreadAlert ? 1 : 0);
    result = 31 * result + desktopNotificationDuration;
    result = 31 * result + viewMode;
    result = 31 * result + (hideUsernames ? 1 : 0);
    result = 31 * result + (hideAvatars ? 1 : 0);
    result = 31 * result + (hideFlexTab ? 1 : 0);
    return result;
  }
}
