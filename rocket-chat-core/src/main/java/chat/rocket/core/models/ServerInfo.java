package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

/**
 * Stores information just for required for initializing connectivity manager.
 */
@AutoValue
public abstract class ServerInfo {

  public abstract String getHostname();

  public abstract String getName();

  @Nullable
  public abstract String getSession();

  public abstract boolean isSecure();

  public static Builder builder() {
    return new AutoValue_ServerInfo.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setHostname(String hostname);

    public abstract Builder setName(String name);

    public abstract Builder setSession(String session);

    public abstract Builder setSecure(boolean isSecure);

    public abstract ServerInfo build();
  }
}
