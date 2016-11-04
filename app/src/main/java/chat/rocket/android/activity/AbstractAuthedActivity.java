package chat.rocket.android.activity;

import android.support.v7.app.AppCompatActivity;

import java.util.List;
import java.util.UUID;

import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.service.RocketChatService;
import io.realm.Realm;
import io.realm.RealmResults;
import jp.co.crowdworks.realm_java_helpers.RealmListObserver;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

abstract class AbstractAuthedActivity extends AppCompatActivity {

    private RealmListObserver<ServerConfig> mInsertEmptyRecordIfNoConfigurationExists =
            new RealmListObserver<ServerConfig>() {
                @Override
                protected RealmResults<ServerConfig> queryItems(Realm realm) {
                    return realm.where(ServerConfig.class).findAll();
                }

                @Override
                protected void onCollectionChanged(List<ServerConfig> list) {
                    if (list.isEmpty()) {
                        final String newId = UUID.randomUUID().toString();
                        RealmHelperBolts
                                .executeTransaction(realm ->
                                        realm.createObject(ServerConfig.class, newId))
                                .continueWith(new LogcatIfError());
                    }
                }
            };

    private RealmListObserver<ServerConfig> mShowConfigActivityIfNeeded =
            new RealmListObserver<ServerConfig>() {
                @Override
                protected RealmResults<ServerConfig> queryItems(Realm realm) {
                    return ServerConfig.queryLoginRequiredConnections(realm).findAll();
                }

                @Override
                protected void onCollectionChanged(List<ServerConfig> list) {
                    ServerConfigActivity.launchFor(AbstractAuthedActivity.this, list);
                }
            };

    @Override
    protected void onResume() {
        super.onResume();
        RocketChatService.keepalive(this);
        mInsertEmptyRecordIfNoConfigurationExists.sub();
        mShowConfigActivityIfNeeded.sub();
    }

    @Override
    protected void onPause() {
        mShowConfigActivityIfNeeded.unsub();
        mInsertEmptyRecordIfNoConfigurationExists.unsub();
        super.onPause();
    }
}
