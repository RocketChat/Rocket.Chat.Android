package chat.rocket.android.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.RocketChatCache;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import chat.rocket.android.push.PushConstants;
import chat.rocket.android.push.PushNotificationHandler;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.core.models.ServerInfo;
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
      } else {
        recoverFromHostnameError(prefs);
      }
    } else {
      if (hostname.equals(newHostname)) {
        // we are good
        return;
      }

      if (assertServerRealmStoreExists(newHostname)) {
        updateHostname(newHostname);
      } else {
        recoverFromHostnameError(prefs);
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

  private void recoverFromHostnameError(SharedPreferences prefs) {
    final List<ServerInfo> serverInfoList =
        ConnectivityManager.getInstance(getApplicationContext()).getServerList();
    if (serverInfoList == null || serverInfoList.size() == 0) {
      LaunchUtil.showAddServerActivity(this);
      return;
    }

    // just connect to the first available
    final ServerInfo serverInfo = serverInfoList.get(0);
    prefs.edit()
        .putString(RocketChatCache.KEY_SELECTED_SERVER_HOSTNAME, serverInfo.getHostname())
        .remove(RocketChatCache.KEY_SELECTED_ROOM_ID)
        .apply();
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

    RealmRoom room = RealmStore.get(hostname).executeTransactionForRead(realm ->
        realm.where(RealmRoom.class).equalTo(RealmRoom.ROOM_ID, roomId).findFirst());
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
