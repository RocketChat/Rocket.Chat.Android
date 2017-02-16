package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Settings {

  public abstract String getId();

  public abstract Preferences getPreferences();

  public static Builder builder() {
    return new AutoValue_Settings.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setPreferences(Preferences preferences);

    public abstract Settings build();
  }
}
