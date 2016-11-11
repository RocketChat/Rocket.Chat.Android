package chat.rocket.android.model.ddp;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * User.
 */
@SuppressWarnings({"PMD.ShortClassName", "PMD.ShortVariable",
    "PMD.MethodNamingConventions", "PMD.VariableNamingConventions"})
public class User extends RealmObject {
  @PrimaryKey private String _id;
  private String username;
  private String status;
  private double utcOffset;
  private RealmList<Email> emails;

  public String get_id() {
    return _id;
  }

  public void set_id(String _id) {
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
}
