package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AttachmentAuthor {

  public abstract String getName();

  public abstract String getLink();

  public abstract String getIconUrl();

  public static Builder builder() {
    return new AutoValue_AttachmentAuthor.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setName(String name);

    public abstract Builder setLink(String link);

    public abstract Builder setIconUrl(String iconUrl);

    public abstract AttachmentAuthor build();
  }
}
