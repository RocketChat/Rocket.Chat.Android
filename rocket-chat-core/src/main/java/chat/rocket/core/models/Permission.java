package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class Permission {

  public abstract String getId();

  public abstract String getName();

  public abstract List<Role> getRoles();

  public static Builder builder() {
    return new AutoValue_Permission.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setRoles(List<Role> roles);

    public abstract Permission build();
  }
}
