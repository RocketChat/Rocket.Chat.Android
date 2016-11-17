package chat.rocket.android.model.internal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Load messages in the room.
 */
public class LoadMessageProcedure extends RealmObject {
  @PrimaryKey private String roomId;
  private int syncstate;

  private boolean reset;
  private long timestamp;
  private int count;

  private boolean hasNext;

  public String getRoomId() {
    return roomId;
  }

  public void setRoomId(String roomId) {
    this.roomId = roomId;
  }

  public int getSyncstate() {
    return syncstate;
  }

  public void setSyncstate(int syncstate) {
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

  public boolean isHasNext() {
    return hasNext;
  }

  public void setHasNext(boolean hasNext) {
    this.hasNext = hasNext;
  }
}
