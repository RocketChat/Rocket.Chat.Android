package chat.rocket.persistence.realm;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmConfiguration;

import chat.rocket.persistence.realm.modules.RocketChatLibraryModule;

public class RocketChatPersistenceRealm {

  public static void init(Context context) {
    Realm.init(context);

    Realm.setDefaultConfiguration(
        new RealmConfiguration.Builder()
            .name("rocket.chat.persistence.realm")
            .modules(new RocketChatLibraryModule())
            .deleteRealmIfMigrationNeeded()
            .build());
  }
}
