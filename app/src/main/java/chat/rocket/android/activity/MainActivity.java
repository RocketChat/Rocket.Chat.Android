package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import chat.rocket.android.R;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ServerConfig;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class MainActivity extends AbstractAuthedActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            RealmHelperBolts.executeTransaction(realm -> {
                for (ServerConfig config: ServerConfig.queryActiveConnections(realm).findAll()) {
                    config.setTokenVerified(false);
                }
                return null;
            }).continueWith(new LogcatIfError());
        }
    }
}
