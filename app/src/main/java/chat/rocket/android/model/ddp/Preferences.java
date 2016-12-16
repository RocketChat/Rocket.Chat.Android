package chat.rocket.android.model.ddp;

import io.realm.RealmObject;

public class Preferences extends RealmObject {

  private boolean newRoomNotification;
  private boolean newMessageNotification;
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

  public boolean isNewRoomNotification() {
    return newRoomNotification;
  }

  public boolean isNewMessageNotification() {
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
}
