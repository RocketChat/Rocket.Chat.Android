package chat.rocket.android.activity;

import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.service.RocketChatService;
import io.realm.Realm;
import io.realm.RealmResults;
import java.util.List;
import java.util.UUID;
import jp.co.crowdworks.realm_java_helpers.RealmListObserver;
import jp.co.crowdworks.realm_java_helpers_bolts.RealmHelperBolts;

abstract class AbstractAuthedActivity extends AbstractFragmentActivity {
  private RealmListObserver<ServerConfig> serverConfigEmptinessObserver =
      new RealmListObserver<ServerConfig>() {
        @Override protected RealmResults<ServerConfig> queryItems(Realm realm) {
          return realm.where(ServerConfig.class).findAll();
        }

        @Override protected void onCollectionChanged(List<ServerConfig> list) {
          if (list.isEmpty()) {
            final String newId = UUID.randomUUID().toString();
            RealmHelperBolts.executeTransaction(
                realm -> realm.createObject(ServerConfig.class, newId))
                .continueWith(new LogcatIfError());
          }
        }
      };

  private RealmListObserver<ServerConfig> loginRequiredServerConfigObserver =
      new RealmListObserver<ServerConfig>() {
        @Override protected RealmResults<ServerConfig> queryItems(Realm realm) {
          return ServerConfig.queryLoginRequiredConnections(realm).findAll();
        }

        @Override protected void onCollectionChanged(List<ServerConfig> list) {
          ServerConfigActivity.launchFor(AbstractAuthedActivity.this, list);
        }
      };

  @Override protected void onResume() {
    super.onResume();
    RocketChatService.keepalive(this);
    serverConfigEmptinessObserver.sub();
    loginRequiredServerConfigObserver.sub();
  }

  @Override protected void onPause() {
    loginRequiredServerConfigObserver.unsub();
    serverConfigEmptinessObserver.unsub();
    super.onPause();
  }
}
