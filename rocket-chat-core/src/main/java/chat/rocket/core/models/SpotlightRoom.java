package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class SpotlightRoom {

  public abstract String getId();

  public abstract String getName();

  public abstract String getType();

  public boolean isChannel() {
    return Room.TYPE_CHANNEL.equals(getType());
  }

  public boolean isPrivate() {
    return Room.TYPE_GROUP.equals(getType());
  }

  public boolean isDirectMessage() {
    return Room.TYPE_DIRECT_MESSAGE.equals(getType());
  }

  public static Builder builder() {
    return new AutoValue_SpotlightRoom.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setType(String type);

    public abstract SpotlightRoom build();
  }
}
