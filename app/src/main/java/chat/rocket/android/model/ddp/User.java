package chat.rocket.android.model.ddp;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.annotations.PrimaryKey;

/**
 * User.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class User extends RealmObject {

  public static final String ID = "_id";
  public static final String USERNAME = "username";
  public static final String STATUS = "status";
  public static final String UTC_OFFSET = "utcOffset";
  public static final String EMAILS = "emails";
  public static final String SETTINGS = "settings";

  public static final String STATUS_ONLINE = "online";
  public static final String STATUS_BUSY = "busy";
  public static final String STATUS_AWAY = "away";
  public static final String STATUS_OFFLINE = "offline";

  @PrimaryKey private String _id;
  private String username;
  private String status;
  private double utcOffset;
  private RealmList<Email> emails;
  private Settings settings;

  public static RealmQuery<User> queryCurrentUser(Realm realm) {
    return realm.where(User.class).isNotEmpty(EMAILS);
  }

  public String getId() {
    return _id;
  }

  public void setId(String _id) {
    this._id = _id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public double getUtcOffset() {
    return utcOffset;
  }

  public void setUtcOffset(double utcOffset) {
    this.utcOffset = utcOffset;
  }

  public RealmList<Email> getEmails() {
    return emails;
  }

  public void setEmails(RealmList<Email> emails) {
    this.emails = emails;
  }

  public Settings getSettings() {
    return settings;
  }

  @Override
  public String toString() {
    return "User{" +
        "_id='" + _id + '\'' +
        ", username='" + username + '\'' +
        ", status='" + status + '\'' +
        ", utcOffset=" + utcOffset +
        ", emails=" + emails +
        ", settings=" + settings +
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

    User user = (User) o;

    if (Double.compare(user.utcOffset, utcOffset) != 0) {
      return false;
    }
    if (_id != null ? !_id.equals(user._id) : user._id != null) {
      return false;
    }
    if (username != null ? !username.equals(user.username) : user.username != null) {
      return false;
    }
    if (status != null ? !status.equals(user.status) : user.status != null) {
      return false;
    }
    if (emails != null ? !emails.equals(user.emails) : user.emails != null) {
      return false;
    }
    return settings != null ? settings.equals(user.settings) : user.settings == null;

  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = _id != null ? _id.hashCode() : 0;
    result = 31 * result + (username != null ? username.hashCode() : 0);
    result = 31 * result + (status != null ? status.hashCode() : 0);
    temp = Double.doubleToLongBits(utcOffset);
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (emails != null ? emails.hashCode() : 0);
    result = 31 * result + (settings != null ? settings.hashCode() : 0);
    return result;
  }
}
