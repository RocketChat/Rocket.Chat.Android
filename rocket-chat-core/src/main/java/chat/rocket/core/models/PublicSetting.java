package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class PublicSetting {

  public abstract String getId();

  @Nullable
  public abstract String getGroup();

  @Nullable
  public abstract String getType();

  public abstract String getValue();

  public abstract long getUpdatedAt();

  public boolean getValueAsBoolean() {
    return Boolean.parseBoolean(getValue());
  }

  public long getValueAsLong() {
    return Long.parseLong(getValue());
  }

  public static Builder builder() {
    return new AutoValue_PublicSetting.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setGroup(String group);

    public abstract Builder setType(String type);

    public abstract Builder setValue(String value);

    public abstract Builder setUpdatedAt(long updatedAt);

    public abstract PublicSetting build();
  }
}
