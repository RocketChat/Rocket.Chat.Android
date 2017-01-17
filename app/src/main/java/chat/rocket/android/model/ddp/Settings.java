package chat.rocket.android.model.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings({"PMD.ShortVariable"})
public class Settings extends RealmObject {

  @PrimaryKey private String id;

  private Preferences preferences;

  public Preferences getPreferences() {
    return preferences;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Settings settings = (Settings) o;

    if (id != null ? !id.equals(settings.id) : settings.id != null) {
      return false;
    }
    return preferences != null ? preferences.equals(settings.preferences)
        : settings.preferences == null;

  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (preferences != null ? preferences.hashCode() : 0);
    return result;
  }
}
