package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Chat Room.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable", "PMD.VariableNamingConventions"})
public class Room extends RealmObject {
  public static final String TYPE_CHANNEL = "c";
  public static final String TYPE_PRIVATE = "p";
  public static final String TYPE_DIRECT_MESSAGE = "d";

  @PrimaryKey private String _id;
  private String serverConfigId;
  private String name;
  private String t; //type { c: channel, d: direct message, p: private }
  private User u; //User who created this room.
  private String topic;

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
    this._id = _id;
  }

  public String getServerConfigId() {
    return serverConfigId;
  }

  public void setServerConfigId(String serverConfigId) {
    this.serverConfigId = serverConfigId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getT() {
    return t;
  }

  public void setT(String t) {
    this.t = t;
  }

  public User getU() {
    return u;
  }

  public void setU(User u) {
    this.u = u;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }
}
