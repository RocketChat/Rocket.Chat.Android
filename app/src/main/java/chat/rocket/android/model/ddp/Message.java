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

  @SuppressWarnings({"PMD.AvoidFieldNameMatchingTypeName"})
  public static final String MESSAGE = "msg";
  public static final String USER = "u";
  public static final String USER_ID = "u._id";
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

  @Override
  public String toString() {
    return "Message{" +
        "_id='" + _id + '\'' +
        ", t='" + t + '\'' +
        ", rid='" + rid + '\'' +
        ", syncstate=" + syncstate +
        ", ts=" + ts +
        ", msg='" + msg + '\'' +
        ", u=" + u +
        ", groupable=" + groupable +
        ", attachments='" + attachments + '\'' +
        ", urls='" + urls + '\'' +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Message message = (Message) o;

    if (syncstate != message.syncstate) {
      return false;
    }
    if (ts != message.ts) {
      return false;
    }
    if (groupable != message.groupable) {
      return false;
    }
    if (_id != null ? !_id.equals(message._id) : message._id != null) {
      return false;
    }
    if (t != null ? !t.equals(message.t) : message.t != null) {
      return false;
    }
    if (rid != null ? !rid.equals(message.rid) : message.rid != null) {
      return false;
    }
    if (msg != null ? !msg.equals(message.msg) : message.msg != null) {
      return false;
    }
    if (u != null ? !u.equals(message.u) : message.u != null) {
      return false;
    }
    if (attachments != null ? !attachments.equals(message.attachments)
        : message.attachments != null) {
      return false;
    }
    return urls != null ? urls.equals(message.urls) : message.urls == null;

  }

  @Override
  public int hashCode() {
    int result = _id != null ? _id.hashCode() : 0;
    result = 31 * result + (t != null ? t.hashCode() : 0);
    result = 31 * result + (rid != null ? rid.hashCode() : 0);
    result = 31 * result + syncstate;
    result = 31 * result + (int) (ts ^ (ts >>> 32));
    result = 31 * result + (msg != null ? msg.hashCode() : 0);
    result = 31 * result + (u != null ? u.hashCode() : 0);
    result = 31 * result + (groupable ? 1 : 0);
    result = 31 * result + (attachments != null ? attachments.hashCode() : 0);
    result = 31 * result + (urls != null ? urls.hashCode() : 0);
    return result;
  }
}
