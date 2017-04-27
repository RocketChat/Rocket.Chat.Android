package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

@AutoValue
public abstract class RoomRole {

  public abstract String getId();

  public abstract String getRoomId();

  public abstract User getUser();

  public abstract List<Role> getRoles();

  public static Builder builder() {
    return new AutoValue_RoomRole.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setRoomId(String roomId);

    public abstract Builder setUser(User user);

    public abstract Builder setRoles(List<Role> roles);

    public abstract RoomRole build();
  }
}
