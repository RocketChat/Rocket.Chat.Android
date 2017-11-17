package chat.rocket.persistence.realm.models.ddp;

import chat.rocket.core.models.Email;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Login-RealmUser's email.
 */
public class RealmEmail extends RealmObject {
  @PrimaryKey private String address;
  private boolean verified;

  public Email asEmail() {
    return Email.builder()
        .setAddress(address)
        .setVerified(verified)
        .build();
  }

  @SuppressWarnings({"PMD.ShortVariable"})
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RealmEmail email = (RealmEmail) o;

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
