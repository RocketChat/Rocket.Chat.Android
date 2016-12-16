package chat.rocket.android.model.ddp;

import io.realm.RealmObject;

public class Settings extends RealmObject {
  private Preferences preferences;

  public Preferences getPreferences() {
    return preferences;
  }
}
