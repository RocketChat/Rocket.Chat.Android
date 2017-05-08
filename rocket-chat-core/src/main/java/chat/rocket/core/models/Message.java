package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

import java.util.List;
import javax.annotation.Nullable;

@AutoValue
public abstract class Message {

  public abstract String getId();

  @Nullable
  public abstract String getType();

  public abstract String getRoomId();

  public abstract int getSyncState();

  public abstract long getTimestamp();

  public abstract String getMessage();

  @Nullable
  public abstract User getUser();

  public abstract boolean isGroupable();

  @Nullable
  public abstract List<Attachment> getAttachments();

  @Nullable
  public abstract List<WebContent> getWebContents();

  @Nullable
  public abstract String getAlias();

  @Nullable
  public abstract String getAvatar();

  public abstract long getEditedAt();

  public abstract Message withSyncState(int syncState);

  public abstract Message withUser(User user);

  public abstract Message withMessage(String message);

  public abstract Message withEditedAt(long editedAt);

  public static Builder builder() {
    return new AutoValue_Message.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setId(String id);

    public abstract Builder setType(String type);

    public abstract Builder setRoomId(String roomId);

    public abstract Builder setSyncState(int syncState);

    public abstract Builder setTimestamp(long timestamp);

    public abstract Builder setMessage(String message);

    public abstract Builder setUser(User user);

    public abstract Builder setGroupable(boolean groupable);

    public abstract Builder setAttachments(List<Attachment> attachments);

    public abstract Builder setWebContents(List<WebContent> webContents);

    public abstract Builder setAlias(String alias);

    public abstract Builder setAvatar(String avatar);

    public abstract Builder setEditedAt(long editedAt);

    public abstract Message build();
  }
}
