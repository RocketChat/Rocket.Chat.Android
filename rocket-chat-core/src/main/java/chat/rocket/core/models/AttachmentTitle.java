package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import javax.annotation.Nullable;

@AutoValue
public abstract class AttachmentTitle {

  public abstract String getTitle();

  @Nullable
  public abstract String getLink();

  @Nullable
  public abstract String getDownloadLink();

  public static Builder builder() {
    return new AutoValue_AttachmentTitle.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setTitle(String title);

    public abstract Builder setLink(String link);

    public abstract Builder setDownloadLink(String downloadLink);

    public abstract AttachmentTitle build();
  }
}
