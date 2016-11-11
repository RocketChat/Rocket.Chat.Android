package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Login-User's email
 */
public class Email extends RealmObject {
  @PrimaryKey private String address;
  private boolean verified;
}
