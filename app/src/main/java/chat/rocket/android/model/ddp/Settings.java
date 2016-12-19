package chat.rocket.android.model.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Settings extends RealmObject {

  @PrimaryKey private String id;

  private Preferences preferences;

  public Preferences getPreferences() {
    return preferences;
  }
}
