package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class WebContentMeta {

  public enum Type {
    OPEN_GRAPH, TWITTER, OTHER
  }

  public abstract Type getType();

  @Nullable
  public abstract String getTitle();

  @Nullable
  public abstract String getDescription();

  @Nullable
  public abstract String getImage();

  public static Builder builder() {
    return new AutoValue_WebContentMeta.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setType(Type type);

    public abstract Builder setTitle(String title);

    public abstract Builder setDescription(String description);

    public abstract Builder setImage(String image);

    public abstract WebContentMeta build();
  }
}
