package chat.rocket.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import chat.rocket.android.RocketChatCache;
import chat.rocket.android.model.ddp.RoomSubscription;
import chat.rocket.android.push.PushConstants;
import chat.rocket.android.push.PushNotificationHandler;
import chat.rocket.android.realm_helper.RealmListObserver;
import chat.rocket.android.realm_helper.RealmStore;
import chat.rocket.android.service.ConnectivityManager;
import icepick.State;

abstract class AbstractAuthedActivity extends AbstractFragmentActivity {
  @State protected String hostname;
  @State protected String roomId;
  SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener =
      (sharedPreferences, key) -> {
        if (RocketChatCache.KEY_SELECTED_SERVER_HOSTNAME.equals(key)) {
          updateHostnameIfNeeded(sharedPreferences);
        } else if (RocketChatCache.KEY_SELECTED_ROOM_ID.equals(key)) {
          updateRoomIdIfNeeded(sharedPreferences);
        }
      };

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    if (savedInstanceState == null) {
      handleIntent(getIntent());
    }
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handleIntent(intent);
  }

  private void handleIntent(Intent intent) {
    if (intent == null) {
      return;
    }

    if (intent.hasExtra(PushConstants.HOSTNAME)) {
      SharedPreferences.Editor editor = RocketChatCache.get(this).edit();
      editor.putString(RocketChatCache.KEY_SELECTED_SERVER_HOSTNAME,
          intent.getStringExtra(PushConstants.HOSTNAME));

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

  private void updateHostnameIfNeeded(SharedPreferences prefs) {
    String newHostname = prefs.getString(RocketChatCache.KEY_SELECTED_SERVER_HOSTNAME, null);
    if (hostname == null) {
      if (newHostname != null && assertServerRealmStoreExists(newHostname)) {
        updateHostname(newHostname);
      }
    } else {
      if (!hostname.equals(newHostname) && assertServerRealmStoreExists(newHostname)) {
        updateHostname(newHostname);
      }
    }
  }

  private boolean assertServerRealmStoreExists(String hostname) {
    return RealmStore.get(hostname) != null;
  }

  private void updateHostname(String hostname) {
    this.hostname = hostname;
    onHostnameUpdated();
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
    if (!assertServerRealmStoreExists(hostname)) {
      return false;
    }

    RoomSubscription room = RealmStore.get(hostname).executeTransactionForRead(realm ->
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

  protected void onHostnameUpdated() {
  }

  protected void onRoomIdUpdated() {
  }

  @Override
  protected void onResume() {
    super.onResume();
    ConnectivityManager.getInstance(getApplicationContext()).keepAliveServer();

    SharedPreferences prefs = RocketChatCache.get(this);
    updateHostnameIfNeeded(prefs);
    updateRoomIdIfNeeded(prefs);
    prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
  }

  @Override
  protected void onPause() {
    SharedPreferences prefs = RocketChatCache.get(this);
    prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);

    super.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }
}
