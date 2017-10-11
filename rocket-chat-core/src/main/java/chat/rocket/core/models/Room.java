package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Room {

  public static final String TYPE_CHANNEL = "c";
  public static final String TYPE_GROUP = "p";
  public static final String TYPE_DIRECT_MESSAGE = "d";
  public static final String TYPE_LIVECHAT = "l";

  public abstract String getId();

  public abstract String getRoomId();

  public abstract String getName();

  public abstract String getType();

  public abstract boolean isOpen();

  public abstract boolean isAlert();

  public abstract int getUnread();

  public abstract long getUpdatedAt();

  public abstract long getLastSeen();

  public abstract boolean isFavorite();

  public boolean isChannel() {
    return TYPE_CHANNEL.equals(getType());
  }

  public boolean isPrivate() {
    return TYPE_GROUP.equals(getType());
  }

  public boolean isDirectMessage() {
    return TYPE_DIRECT_MESSAGE.equals(getType());
  }

  public boolean isLivechat() {
    return TYPE_LIVECHAT.equals(getType());
  }

  public static Builder builder() {
    return new AutoValue_Room.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setRoomId(String roomId);

    public abstract Builder setName(String name);

    public abstract Builder setType(String type);

    public abstract Builder setOpen(boolean open);

    public abstract Builder setAlert(boolean alert);

    public abstract Builder setUnread(int unread);

    public abstract Builder setUpdatedAt(long updatedAt);

    public abstract Builder setLastSeen(long lastSeen);

    public abstract Builder setFavorite(boolean favorite);

    public abstract Room build();
  }
}
