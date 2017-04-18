package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class SpotlightUser {

  public abstract String getId();

  public abstract String getUsername();

  @Nullable
  public abstract String getName();

  @Nullable
  public abstract String getStatus();

  public static Builder builder() {
    return new AutoValue_SpotlightUser.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setUsername(String username);

    public abstract Builder setName(String name);

    public abstract Builder setStatus(String status);

    public abstract SpotlightUser build();
  }
}
