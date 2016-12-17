package chat.rocket.android.model.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Chat Room(Subscription).
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class RoomSubscription extends RealmObject {
  public static final String TYPE_CHANNEL = "c";
  public static final String TYPE_PRIVATE = "p";
  public static final String TYPE_DIRECT_MESSAGE = "d";

  private String _id; //subscriptionId
  @PrimaryKey private String rid; //roomId
  private String name;
  //private User u; // REMARK: do not save u, because it is just me.
  private String t; //type { c: channel, d: direct message, p: private }
  private boolean open;
  private boolean alert;
  private int unread;
  private long _updatedAt;
  private long ls; //last seen.

  public static JSONObject customizeJson(JSONObject roomSubscriptionJson) throws JSONException {
    if (!roomSubscriptionJson.isNull("ls")) {
      long ls = roomSubscriptionJson.getJSONObject("ls").getLong("$date");
      roomSubscriptionJson.remove("ls");
      roomSubscriptionJson.put("ls", ls);
    }

    if (!roomSubscriptionJson.isNull("_updatedAt")) {
      long updatedAt = roomSubscriptionJson.getJSONObject("_updatedAt").getLong("$date");
      roomSubscriptionJson.remove("_updatedAt");
      roomSubscriptionJson.put("_updatedAt", updatedAt);
    }

    return roomSubscriptionJson;
  }

  public String getId() {
    return _id;
  }

  public void setId(String _id) {
    this._id = _id;
  }

  public String getRoomId() {
    return rid;
  }

  public void setRoomId(String rid) {
    this.rid = rid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getType() {
    return t;
  }

  public void setType(String t) {
    this.t = t;
  }

  public boolean isOpen() {
    return open;
  }

  public void setOpen(boolean open) {
    this.open = open;
  }

  public boolean isAlert() {
    return alert;
  }

  public void setAlert(boolean alert) {
    this.alert = alert;
  }

  public int getUnread() {
    return unread;
  }

  public void setUnread(int unread) {
    this.unread = unread;
  }

  public long getUpdatedAt() {
    return _updatedAt;
  }

  public void setUpdatedAt(long _updatedAt) {
    this._updatedAt = _updatedAt;
  }

  public long getLastSeen() {
    return ls;
  }

  public void setLastSeen(long ls) {
    this.ls = ls;
  }
}
