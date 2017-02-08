package chat.rocket.android.model.core;

import com.google.auto.value.AutoValue;

import android.support.annotation.Nullable;

@AutoValue
public abstract class WebContent {

  public abstract String getUrl();

  @Nullable
  public abstract String getMeta();

  @Nullable
  public abstract String getHeaders();

  public static Builder builder() {
    return new AutoValue_WebContent.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setUrl(String url);

    public abstract Builder setMeta(String meta);

    public abstract Builder setHeaders(String headers);

    public abstract WebContent build();
  }

}
