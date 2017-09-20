package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class User {

  public static final String STATUS_ONLINE = "online";
  public static final String STATUS_BUSY = "busy";
  public static final String STATUS_AWAY = "away";
  public static final String STATUS_OFFLINE = "offline";

  public abstract String getId();

  @Nullable
  public abstract String getName();

  @Nullable
  public abstract String getUsername();

  @Nullable
  public abstract String getStatus();

  public abstract double getUtcOffset();

  @Nullable
  public abstract List<Email> getEmails();

  @Nullable
  public abstract Settings getSettings();

  public static Builder builder() {
    return new AutoValue_User.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setName(String name);

    public abstract Builder setUsername(String username);

    public abstract Builder setStatus(String status);

    public abstract Builder setUtcOffset(double utcOffset);

    public abstract Builder setEmails(List<Email> emails);

    public abstract Builder setSettings(Settings settings);

    public abstract User build();
  }
}
