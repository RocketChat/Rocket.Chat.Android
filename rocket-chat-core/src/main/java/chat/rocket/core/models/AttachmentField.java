package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class AttachmentField {

  public abstract boolean isShort();

  public abstract String getTitle();

  public abstract String getText();

  public static Builder builder() {
    return new AutoValue_AttachmentField.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setShort(boolean isShort);

    public abstract Builder setTitle(String link);

    public abstract Builder setText(String text);

    public abstract AttachmentField build();
  }
}
