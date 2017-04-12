package chat.rocket.persistence.realm;

import android.content.Context;
import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RocketChatPersistenceRealm {

  public static void init(Context context) {
    Realm.init(context);

    Realm.setDefaultConfiguration(new RealmConfiguration.Builder().build());
  }
}
