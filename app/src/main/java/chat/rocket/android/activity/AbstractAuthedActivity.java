package chat.rocket.android.activity;

import android.content.SharedPreferences;
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
  protected String roomId;

  SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
      (sharedPreferences, key) -> {
        if (RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID.equals(key)) {
          updateServerConfigIdIfNeeded(sharedPreferences);
        } else if (RocketChatCache.KEY_SELECTED_ROOM_ID.equals(key)) {
          updateRoomIdIfNeeded(sharedPreferences);
        }
      };

  private void updateServerConfigIdIfNeeded(SharedPreferences prefs) {
    String newServerConfigId = prefs.getString(RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID, null);
    if (serverConfigId == null) {
      if (newServerConfigId != null) {
        updateServerConfigId(newServerConfigId);
      }
    } else {
      if (!serverConfigId.equals(newServerConfigId)) {
        updateServerConfigId(newServerConfigId);
      }
    }
  }

  private void updateServerConfigId(String serverConfigId) {
    this.serverConfigId = serverConfigId;
    onServerConfigIdUpdated();
  }

  private void updateRoomIdIfNeeded(SharedPreferences prefs) {
    String newRoomId = prefs.getString(RocketChatCache.KEY_SELECTED_ROOM_ID, null);
    if (roomId == null) {
      if (newRoomId != null) {
        updateRoomId(newRoomId);
      }
    } else {
      if (!roomId.equals(newRoomId)) {
        updateRoomId(newRoomId);
      }
    }
  }

  private void updateRoomId(String roomId) {
    this.roomId = roomId;
    onRoomIdUpdated();
  }

  protected void onServerConfigIdUpdated() {
    RocketChatCache.get(this).edit()
        .remove(RocketChatCache.KEY_SELECTED_ROOM_ID)
        .apply();
  }

  protected void onRoomIdUpdated() {}

  @Override protected void onResume() {
    super.onResume();
    RocketChatService.keepalive(this);
    unconfiguredServersObserver.sub();

    SharedPreferences prefs = RocketChatCache.get(this);
    updateServerConfigIdIfNeeded(prefs);
    updateRoomIdIfNeeded(prefs);
    prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
  }

  @Override protected void onPause() {
    SharedPreferences prefs = RocketChatCache.get(this);
    prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);

    unconfiguredServersObserver.unsub();
    super.onPause();
  }
}
