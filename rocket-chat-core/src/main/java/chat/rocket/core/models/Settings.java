package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class Settings {

  public abstract String getId();

  @Nullable
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
