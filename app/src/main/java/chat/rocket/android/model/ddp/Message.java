package chat.rocket.android.model.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import org.json.JSONException;
import org.json.JSONObject;

import chat.rocket.android.model.SyncState;

/**
 * Message.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class Message extends RealmObject {
  //ref: Rocket.Chat:packages/rocketchat-lib/lib/MessageTypes.coffee

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
    long ts = messageJson.getJSONObject("ts").getLong("$date");
    messageJson.remove("ts");
    messageJson.put("ts", ts).put("syncstate", SyncState.SYNCED);

    if (messageJson.isNull("groupable")) {
      messageJson.put("groupable", true);
    }

    return messageJson;
  }

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public String getT() {
    return t;
  }

  public void setT(String t) {
    this.t = t;
  }

  public String getRid() {
    return rid;
  }

  public void setRid(String rid) {
    this.rid = rid;
  }

  public int getSyncstate() {
    return syncstate;
  }

  public void setSyncstate(int syncstate) {
    this.syncstate = syncstate;
  }

  public long getTs() {
    return ts;
  }

  public void setTs(long ts) {
    this.ts = ts;
  }

  public String getMsg() {
    return msg;
  }

  public void setMsg(String msg) {
    this.msg = msg;
  }

  public User getU() {
    return u;
  }

  public void setU(User u) {
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
