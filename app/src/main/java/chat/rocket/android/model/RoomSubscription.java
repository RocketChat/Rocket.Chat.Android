package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

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
  private String serverConfigId;
  @PrimaryKey private String rid; //roomId
  private String name;
  //private User u; // REMARK: do not save u, because it is just me.
  private String t; //type { c: channel, d: direct message, p: private }
  private boolean open;
  private boolean alert;
  private int unread;

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

  public String getRid() {
    return rid;
  }

  public void setRid(String rid) {
    this.rid = rid;
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
}
