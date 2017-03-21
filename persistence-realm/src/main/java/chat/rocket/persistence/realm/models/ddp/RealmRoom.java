package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.core.JsonConstants;
import chat.rocket.core.models.Room;

/**
 * Chat Room(Subscription).
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class RealmRoom extends RealmObject {

  public static final String ID = "_id";
  public static final String ROOM_ID = "rid";
  public static final String NAME = "name";
  public static final String TYPE = "t";
  public static final String OPEN = "open";
  public static final String ALERT = "alert";
  public static final String UNREAD = "unread";
  public static final String UPDATED_AT = "_updatedAt";
  public static final String LAST_SEEN = "ls";
  public static final String FAVORITE = "f";

  public static final String TYPE_CHANNEL = "c";
  public static final String TYPE_PRIVATE = "p";
  public static final String TYPE_DIRECT_MESSAGE = "d";

  private String _id; //subscriptionId
  @PrimaryKey private String rid; //roomId
  private String name;
  //private RealmUser u; // REMARK: do not save u, because it is just me.
  private String t; //type { c: channel, d: direct message, p: private }
  private boolean open;
  private boolean alert;
  private int unread;
  private long _updatedAt;
  private long ls; //last seen.
  private boolean f;

  public static JSONObject customizeJson(JSONObject roomSubscriptionJson) throws JSONException {
    if (!roomSubscriptionJson.isNull(LAST_SEEN)) {
      long ls = roomSubscriptionJson.getJSONObject(LAST_SEEN).getLong(JsonConstants.DATE);
      roomSubscriptionJson.remove(LAST_SEEN);
      roomSubscriptionJson.put(LAST_SEEN, ls);
    }

    if (!roomSubscriptionJson.isNull(UPDATED_AT)) {
      long updatedAt = roomSubscriptionJson.getJSONObject(UPDATED_AT).getLong(JsonConstants.DATE);
      roomSubscriptionJson.remove(UPDATED_AT);
      roomSubscriptionJson.put(UPDATED_AT, updatedAt);
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

  public boolean isFavorite() {
    return f;
  }

  public void setFavorite(boolean f) {
    this.f = f;
  }

  public Room asRoom() {
    return Room.builder()
        .setId(_id)
        .setRoomId(rid)
        .setName(name)
        .setType(t)
        .setOpen(open)
        .setAlert(alert)
        .setUnread(unread)
        .setUpdatedAt(_updatedAt)
        .setLastSeen(ls)
        .setFavorite(f)
        .build();
  }
}
