package chat.rocket.persistence.realm.models.internal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import chat.rocket.core.models.RoomHistoryState;

/**
 * Load messages in the room.
 */
public class LoadMessageProcedure extends RealmObject {

  @SuppressWarnings({"PMD.ShortVariable"})
  public static final String ID = "roomId";
  public static final String SYNC_STATE = "syncstate";
  public static final String RESET = "reset";
  public static final String TIMESTAMP = "timestamp";
  public static final String COUNT = "count";
  public static final String HAS_NEXT = "hasNext";

  @PrimaryKey private String roomId;
  private int syncstate;
  private boolean reset;
  private long timestamp;
  private int count;

  @SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName"})
  private boolean hasNext;

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public int getSyncState() {
    return syncstate;
  }

  public void setSyncState(int syncstate) {
    this.syncstate = syncstate;
  }

  public boolean isReset() {
    return reset;
  }

  public void setReset(boolean reset) {
    this.reset = reset;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(long timestamp) {
    this.timestamp = timestamp;
  }

  public int getCount() {
    return count;
  }

  public void setCount(int count) {
    this.count = count;
  }

  public boolean hasNext() {
    return hasNext;
  }

  public void setHasNext(boolean hasNext) {
    this.hasNext = hasNext;
  }

  public RoomHistoryState asRoomHistoryState() {
    return RoomHistoryState.builder()
        .setRoomId(roomId)
        .setSyncState(syncstate)
        .setReset(reset)
        .setTimestamp(timestamp)
        .setCount(count)
        .setComplete(!hasNext)
        .build();
  }
}
