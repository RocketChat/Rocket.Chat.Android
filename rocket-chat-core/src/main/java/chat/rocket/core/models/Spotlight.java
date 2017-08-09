package chat.rocket.core.models;

import com.google.auto.value.AutoValue;
import javax.annotation.Nullable;

@AutoValue
public abstract class Spotlight {

  public abstract String getId();

  public abstract String getName();

  @Nullable
  public abstract String getUsername();

  @Nullable
  public abstract String getStatus();

  @Nullable
  public abstract String getType();

  @Nullable
  public boolean isChannel() {
    return Room.TYPE_CHANNEL.equals(getType());
  }

  @Nullable
  public boolean isPrivate() {
    return Room.TYPE_PRIVATE.equals(getType());
  }

  @Nullable
  public boolean isDirectMessage() {
    return Room.TYPE_DIRECT_MESSAGE.equals(getType());
  }

  public static Spotlight.Builder builder() {
    return new AutoValue_Spotlight.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setUsername(String username);

    public abstract Builder setStatus(String status);

    public abstract Builder setType(String type);

    public abstract Spotlight build();
  }
}