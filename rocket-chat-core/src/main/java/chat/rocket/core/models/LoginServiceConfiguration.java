package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class LoginServiceConfiguration {

  public abstract String getId();

  public abstract String getService();

  @Nullable
  public abstract String getKey();

  public static Builder builder() {
    return new AutoValue_LoginServiceConfiguration.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setService(String service);

    public abstract Builder setKey(String key);

    public abstract LoginServiceConfiguration build();
  }
}
