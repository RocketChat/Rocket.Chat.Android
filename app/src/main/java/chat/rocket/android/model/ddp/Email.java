package chat.rocket.android.model.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Login-User's email.
 */
public class Email extends RealmObject {
  @PrimaryKey private String address;
  private boolean verified;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Email email = (Email) o;

    if (verified != email.verified) {
      return false;
    }
    return address != null ? address.equals(email.address) : email.address == null;

  }

  @Override
  public int hashCode() {
    int result = address != null ? address.hashCode() : 0;
    result = 31 * result + (verified ? 1 : 0);
    return result;
  }
}
