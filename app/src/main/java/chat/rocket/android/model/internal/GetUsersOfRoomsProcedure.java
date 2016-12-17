package chat.rocket.android.model.internal;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Get users in a Room.
 */
public class GetUsersOfRoomsProcedure extends RealmObject {
  @PrimaryKey private String roomId;
  private int syncstate;
  private boolean showAll;

  private long total;
  private String records;

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

  public boolean isShowAll() {
    return showAll;
  }

  public void setShowAll(boolean showAll) {
    this.showAll = showAll;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public String getRecords() {
    return records;
  }

  public void setRecords(String records) {
    this.records = records;
  }
}
