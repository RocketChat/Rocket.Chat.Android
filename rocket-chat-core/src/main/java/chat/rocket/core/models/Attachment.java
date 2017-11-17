package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;

import javax.annotation.Nullable;

@AutoValue
public abstract class Attachment {

  @Nullable
  public abstract String getColor();

  @Nullable
  public abstract String getText();

  @Nullable
  public abstract String getTimestamp();

  @Nullable
  public abstract String getThumbUrl();

  @Nullable
  public abstract String getMessageLink();

  public abstract boolean isCollapsed();

  @Nullable
  public abstract AttachmentAuthor getAttachmentAuthor();

  @Nullable
  public abstract AttachmentTitle getAttachmentTitle();

  @Nullable
  public abstract String getImageUrl();

  @Nullable
  public abstract String getAudioUrl();

  @Nullable
  public abstract String getVideoUrl();

  @Nullable
  public abstract List<AttachmentField> getAttachmentFields();

  public static Builder builder() {
    return new AutoValue_Attachment.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setColor(String color);

    public abstract Builder setText(String text);

    public abstract Builder setTimestamp(String timestamp);

    public abstract Builder setThumbUrl(String thumbUrl);

    public abstract Builder setMessageLink(String messageLink);

    public abstract Builder setCollapsed(boolean collapsed);

    public abstract Builder setAttachmentAuthor(AttachmentAuthor attachmentAuthor);

    public abstract Builder setAttachmentTitle(AttachmentTitle attachmentTitle);

    public abstract Builder setImageUrl(String imageUrl);

    public abstract Builder setAudioUrl(String audioUrl);

    public abstract Builder setVideoUrl(String videoUrl);

    public abstract Builder setAttachmentFields(List<AttachmentField> attachmentFields);

    public abstract Attachment build();
  }
}
