package chat.rocket.persistence.realm.models.ddp;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import chat.rocket.core.models.Settings;

@SuppressWarnings({"PMD.ShortVariable"})
public class RealmSettings extends RealmObject {

  @PrimaryKey private String id;

  private RealmPreferences preferences;

  public RealmPreferences getPreferences() {
    return preferences;
  }

  public Settings asSettings() {
    return Settings.builder()
        .setId(id)
        .setPreferences(preferences != null ? preferences.asPreferences() : null)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    RealmSettings settings = (RealmSettings) o;

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
