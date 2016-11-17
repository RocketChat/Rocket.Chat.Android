package chat.rocket.android.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.realm_helper.RealmStore;
import chat.rocket.android.service.RocketChatService;

abstract class AbstractAuthedActivity extends AbstractFragmentActivity {
  private RealmListObserver<ServerConfig> unconfiguredServersObserver =
      RealmStore.getDefault()
          .createListObserver(realm ->
              realm.where(ServerConfig.class).isNotNull("session").findAll())
          .setOnUpdateListener(results -> {
            if (results.isEmpty()) {
              LaunchUtil.showAddServerActivity(this);
            }
          });

  protected String serverConfigId;

  SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
      (sharedPreferences, key) -> {
        if (RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID.equals(key)) {
          updateServerConfigIdIfNeeded(sharedPreferences);
        }
      };

  private void updateServerConfigIdIfNeeded(SharedPreferences prefs) {
    String newServerConfigId = prefs.getString(RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID, null);
    if (serverConfigId == null) {
      if (newServerConfigId != null) {
        serverConfigId = newServerConfigId;
        onServerConfigIdUpdated();
      }
    } else {
      if (!serverConfigId.equals(newServerConfigId)) {
        serverConfigId = newServerConfigId;
        onServerConfigIdUpdated();
      }
    }
  }

  protected void onServerConfigIdUpdated() {}

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SharedPreferences prefs = RocketChatCache.get(this);
    updateServerConfigIdIfNeeded(prefs);
  }

  @Override protected void onResume() {
    super.onResume();
    RocketChatService.keepalive(this);
    unconfiguredServersObserver.sub();

    SharedPreferences prefs = RocketChatCache.get(this);
    updateServerConfigIdIfNeeded(prefs);
    prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
  }

  @Override protected void onPause() {
    SharedPreferences prefs = RocketChatCache.get(this);
    prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);

    unconfiguredServersObserver.unsub();
    super.onPause();
  }
}
