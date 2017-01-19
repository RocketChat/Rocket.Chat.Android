package chat.rocket.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import chat.rocket.android.LaunchUtil;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.push.PushConstants;
import chat.rocket.android.push.PushNotificationHandler;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.realm_helper.RealmStore;
import chat.rocket.android.service.RocketChatService;
import icepick.State;

abstract class AbstractAuthedActivity extends AbstractFragmentActivity {
  @State protected String serverConfigId;
  @State protected String roomId;
  SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
      (sharedPreferences, key) -> {
        if (RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID.equals(key)) {
          updateServerConfigIdIfNeeded(sharedPreferences);
        } else if (RocketChatCache.KEY_SELECTED_ROOM_ID.equals(key)) {
          updateRoomIdIfNeeded(sharedPreferences);
        }
      };
  private RealmListObserver<ServerConfig> unconfiguredServersObserver =
      RealmStore.getDefault()
          .createListObserver(realm ->
              realm.where(ServerConfig.class).isNotNull(ServerConfig.SESSION).findAll())
          .setOnUpdateListener(results -> {
            if (results.isEmpty()) {
              LaunchUtil.showAddServerActivity(this);
            }
          });

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      onIntent(getIntent());
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    onIntent(intent);
  }

  private void onIntent(Intent intent) {
    if (intent == null) {
      return;
    }

    if (intent.hasExtra(PushConstants.SERVER_CONFIG_ID)) {
      SharedPreferences.Editor editor = RocketChatCache.get(this).edit();
      editor.putString(RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID,
          intent.getStringExtra(PushConstants.SERVER_CONFIG_ID));

      if (intent.hasExtra(PushConstants.ROOM_ID)) {
        editor.putString(RocketChatCache.KEY_SELECTED_ROOM_ID,
            intent.getStringExtra(PushConstants.ROOM_ID));
      }

      if (intent.hasExtra(PushConstants.NOT_ID)) {
        PushNotificationHandler
            .cleanUpNotificationStack(intent.getIntExtra(PushConstants.NOT_ID, 0));
      }
      editor.apply();
    }
  }

  private void updateServerConfigIdIfNeeded(SharedPreferences prefs) {
    String newServerConfigId = prefs.getString(RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID, null);
    if (serverConfigId == null) {
      if (newServerConfigId != null && assertServerConfigExists(newServerConfigId, prefs)) {
        updateServerConfigId(newServerConfigId);
      }
    } else {
      if (!serverConfigId.equals(newServerConfigId)
          && assertServerConfigExists(newServerConfigId, prefs)) {
        updateServerConfigId(newServerConfigId);
      }
    }
  }

  private boolean assertServerConfigExists(String serverConfigId, SharedPreferences prefs) {
    if (RealmStore.get(serverConfigId) == null) {
      prefs.edit()
          .remove(RocketChatCache.KEY_SELECTED_SERVER_CONFIG_ID)
          .remove(RocketChatCache.KEY_SELECTED_ROOM_ID)
          .apply();
      return false;
    }
    return true;
  }

  private void updateServerConfigId(String serverConfigId) {
    this.serverConfigId = serverConfigId;
    onServerConfigIdUpdated();
  }

  private void updateRoomIdIfNeeded(SharedPreferences prefs) {
    String newRoomId = prefs.getString(RocketChatCache.KEY_SELECTED_ROOM_ID, null);
    if (roomId == null) {
      if (newRoomId != null && assertRoomSubscriptionExists(newRoomId, prefs)) {
        updateRoomId(newRoomId);
      }
    } else {
      if (!roomId.equals(newRoomId) && assertRoomSubscriptionExists(newRoomId, prefs)) {
        updateRoomId(newRoomId);
      }
    }
  }

  private boolean assertRoomSubscriptionExists(String roomId, SharedPreferences prefs) {
    if (!assertServerConfigExists(serverConfigId, prefs)) {
      return false;
    }

    RoomSubscription room = RealmStore.get(serverConfigId).executeTransactionForRead(realm ->
        realm.where(RoomSubscription.class).equalTo(RoomSubscription.ROOM_ID, roomId).findFirst());
    if (room == null) {
      prefs.edit()
          .remove(RocketChatCache.KEY_SELECTED_ROOM_ID)
          .apply();
      return false;
    }
    return true;
  }

  private void updateRoomId(String roomId) {
    this.roomId = roomId;
    onRoomIdUpdated();
  }

  protected void onServerConfigIdUpdated() {
  }

  protected void onRoomIdUpdated() {
  }

  @Override
  protected void onResume() {
    super.onResume();
    RocketChatService.keepAlive(this);
    unconfiguredServersObserver.sub();

    SharedPreferences prefs = RocketChatCache.get(this);
    updateServerConfigIdIfNeeded(prefs);
    updateRoomIdIfNeeded(prefs);
    prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
  }

  @Override
  protected void onPause() {
    SharedPreferences prefs = RocketChatCache.get(this);
    prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);

    unconfiguredServersObserver.unsub();
    super.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }
}
