package chat.rocket.android;

import android.content.Context;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class RealmConfig {
    private static RealmConfiguration sConfig;
    public static void setDefault(Context context) {
        sConfig = new RealmConfiguration.Builder(context)
                .name("default.realm")
                .schemaVersion(1)
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(sConfig);
    }
}