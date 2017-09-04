package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class WebContentParsedUrl {

  @Nullable
  public abstract String getHost();

  public static Builder builder() {
    return new AutoValue_WebContentParsedUrl.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setHost(String host);

    public abstract WebContentParsedUrl build();
  }
}
