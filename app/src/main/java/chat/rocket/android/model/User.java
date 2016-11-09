package chat.rocket.android.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * User.
 */
@SuppressWarnings("PMD.ShortVariable")
public class User extends RealmObject {
  @PrimaryKey private String _id;
  private String username;
}
