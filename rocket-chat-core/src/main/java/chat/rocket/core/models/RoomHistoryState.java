package chat.rocket.core.models;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class RoomHistoryState {

  public abstract String getRoomId();

  public abstract int getSyncState();

  public abstract boolean isReset();

  public abstract long getTimestamp();

  public abstract int getCount();

  public abstract boolean isComplete();

  public abstract RoomHistoryState withSyncState(int syncState);

  public static Builder builder() {
    return new AutoValue_RoomHistoryState.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setRoomId(String roomId);

    public abstract Builder setSyncState(int syncState);

    public abstract Builder setReset(boolean isReset);

    public abstract Builder setTimestamp(long timestamp);

    public abstract Builder setCount(int count);

    public abstract Builder setComplete(boolean isComplete);

    public abstract RoomHistoryState build();
  }
}
