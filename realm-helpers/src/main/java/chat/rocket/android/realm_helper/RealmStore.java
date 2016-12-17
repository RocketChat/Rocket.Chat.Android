package chat.rocket.android.realm_helper;

import io.realm.RealmConfiguration;

import java.util.HashMap;

public class RealmStore {
  public static HashMap<String, RealmConfiguration> sStore = new HashMap<>();

  private static RealmConfiguration createConfigFor(String name) {
    return new RealmConfiguration.Builder()
        .name(name + ".realm")
        .deleteRealmIfMigrationNeeded().build();
  }

  public static void put(String name) {
    sStore.put(name, createConfigFor(name));
  }

  public static RealmHelper getDefault() {
    return new RealmHelper();
  }

  public static RealmHelper get(String name) {
    if (!sStore.containsKey(name)) {
      return null;
    }

    return new RealmHelper(sStore.get(name));
  }

  public static RealmHelper getOrCreate(String name) {
    if (!sStore.containsKey(name)) {
      put(name);
    }
    return new RealmHelper(sStore.get(name));
  }
}
