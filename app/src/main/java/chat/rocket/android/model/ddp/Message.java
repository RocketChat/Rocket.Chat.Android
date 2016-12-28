package chat.rocket.android.model.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.model.JsonConstants;
import chat.rocket.android.model.SyncState;

/**
 * Message.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class Message extends RealmObject {
  //ref: Rocket.Chat:packages/rocketchat-lib/lib/MessageTypes.coffee

  public static final String ID = "_id";
  public static final String TYPE = "t";
  public static final String ROOM_ID = "rid";
  public static final String SYNC_STATE = "syncstate";
  public static final String TIMESTAMP = "ts";
  public static final String MESSAGE = "msg";
  public static final String USER = "u";
  public static final String GROUPABLE = "groupable";
  public static final String ATTACHMENTS = "attachments";
  public static final String URLS = "urls";

  @PrimaryKey private String _id;
  private String t; //type:
  private String rid; //roomId.
  private int syncstate;
  private long ts;
  private String msg;
  private User u;
  private boolean groupable;
  private String attachments; //JSONArray.
  private String urls; //JSONArray.

  public static JSONObject customizeJson(JSONObject messageJson) throws JSONException {
    long ts = messageJson.getJSONObject(TIMESTAMP).getLong(JsonConstants.DATE);
    messageJson.remove(TIMESTAMP);
    messageJson.put(TIMESTAMP, ts).put(SYNC_STATE, SyncState.SYNCED);

    if (messageJson.isNull(GROUPABLE)) {
      messageJson.put(GROUPABLE, true);
    }

    return messageJson;
  }

  public String getId() {
    return _id;
  }

  public void setId(String _id) {
    this._id = _id;
  }

  public String getType() {
    return t;
  }

  public void setType(String t) {
    this.t = t;
  }

  public String getRoomId() {
    return rid;
  }

  public void setRoomId(String rid) {
    this.rid = rid;
  }

  public int getSyncState() {
    return syncstate;
  }

  public void setSyncState(int syncstate) {
    this.syncstate = syncstate;
  }

  public long getTimestamp() {
    return ts;
  }

  public void setTimestamp(long ts) {
    this.ts = ts;
  }

  public String getMessage() {
    return msg;
  }

  public void setMessage(String msg) {
    this.msg = msg;
  }

  public User getUser() {
    return u;
  }

  public void setUser(User u) {
    this.u = u;
  }

  public boolean isGroupable() {
    return groupable;
  }

  public void setGroupable(boolean groupable) {
    this.groupable = groupable;
  }

  public String getAttachments() {
    return attachments;
  }

  public void setAttachments(String attachments) {
    this.attachments = attachments;
  }

  public String getUrls() {
    return urls;
  }

  public void setUrls(String urls) {
    this.urls = urls;
  }
}
