package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Role {

  public abstract String getId();

  public abstract String getName();

  public static Builder builder() {
    return new AutoValue_Role.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Role build();
  }

}
