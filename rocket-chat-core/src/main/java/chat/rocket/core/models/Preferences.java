package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class Preferences {

  public abstract String getId();

  @Nullable
  public abstract String getNewRoomNotification();

  @Nullable
  public abstract String getNewMessageNotification();

  public abstract boolean isUseEmojis();

  public abstract boolean isConvertAsciiEmoji();

  public abstract boolean isSaveMobileBandwidth();

  public abstract boolean isCollapseMediaByDefault();

  public abstract boolean isUnreadRoomsMode();

  public abstract boolean isAutoImageLoad();

  @Nullable
  public abstract String getEmailNotificationMode();

  public abstract boolean isUnreadAlert();

  public abstract int getDesktopNotificationDuration();

  public abstract int getViewMode();

  public abstract boolean isHideUsernames();

  public abstract boolean isHideAvatars();

  public abstract boolean isHideFlexTab();

  public static Builder builder() {
    return new AutoValue_Preferences.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setNewRoomNotification(String newRoomNotification);

    public abstract Builder setNewMessageNotification(String newMessageNotification);

    public abstract Builder setUseEmojis(boolean useEmojis);

    public abstract Builder setConvertAsciiEmoji(boolean convertAsciiEmoji);

    public abstract Builder setSaveMobileBandwidth(boolean saveMobileBandwidth);

    public abstract Builder setCollapseMediaByDefault(boolean collapseMediaByDefault);

    public abstract Builder setUnreadRoomsMode(boolean unreadRoomsMode);

    public abstract Builder setAutoImageLoad(boolean autoImageLoad);

    public abstract Builder setEmailNotificationMode(String emailNotificationMode);

    public abstract Builder setUnreadAlert(boolean unreadAlert);

    public abstract Builder setDesktopNotificationDuration(int desktopNotificationDuration);

    public abstract Builder setViewMode(int viewMode);

    public abstract Builder setHideUsernames(boolean hideUsernames);

    public abstract Builder setHideAvatars(boolean hideAvatars);

    public abstract Builder setHideFlexTab(boolean hideFlexTab);

    public abstract Preferences build();
  }
}
