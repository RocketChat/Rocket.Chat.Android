package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Spotlight {

  public abstract String getId();

  public abstract String getName();

  public abstract String getType();

  public abstract String getStatus();

  public static Spotlight.Builder builder() {
    return new AutoValue_Spotlight.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setType(String type);

    public abstract Builder setStatus(String status);

    public abstract Spotlight build();
  }
}