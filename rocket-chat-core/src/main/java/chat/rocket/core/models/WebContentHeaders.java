package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class WebContentHeaders {

  @Nullable
  public abstract String getContentType();

  public static Builder builder() {
    return new AutoValue_WebContentHeaders.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setContentType(String contentType);

    public abstract WebContentHeaders build();
  }
}
