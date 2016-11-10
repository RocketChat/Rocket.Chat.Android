package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 */
public class Email extends RealmObject {
  @PrimaryKey private String address;
  private boolean verified;
}
