package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hadisatrio.optional.Optional;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import chat.rocket.android.LaunchUtil;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.push.PushConstants;
import chat.rocket.android.push.PushNotificationHandler;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

abstract class AbstractAuthedActivity extends AbstractFragmentActivity {
  @State protected String hostname;
  @State protected String roomId;

  private RocketChatCache rocketChatCache;
  private CompositeDisposable compositeDisposable = new CompositeDisposable();
  private boolean isNotification;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    rocketChatCache = new RocketChatCache(this);

    if (savedInstanceState == null) {
      handleIntent(getIntent());
    }

    updateHostnameIfNeeded(rocketChatCache.getSelectedServerHostname());
    updateRoomIdIfNeeded(rocketChatCache.getSelectedRoomId());
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
      rocketChatCache.setSelectedServerHostname(intent.getStringExtra(PushConstants.HOSTNAME));

      if (intent.hasExtra(PushConstants.ROOM_ID)) {
        rocketChatCache.setSelectedRoomId(intent.getStringExtra(PushConstants.ROOM_ID));
      }
    } else {
      updateHostnameIfNeeded(rocketChatCache.getSelectedServerHostname());
    }

    if (intent.hasExtra(PushConstants.NOT_ID)) {
      isNotification = true;
      PushNotificationHandler
          .cleanUpNotificationStack(intent.getIntExtra(PushConstants.NOT_ID, 0));
    }
  }

  private void updateHostnameIfNeeded(String newHostname) {
    if (hostname == null) {
      if (newHostname != null && assertServerRealmStoreExists(newHostname)) {
        updateHostname(newHostname);
      } else {
        recoverFromHostnameError();
      }
    } else {
      if (hostname.equals(newHostname)) {
        updateHostname(newHostname);
        return;
      }

      if (assertServerRealmStoreExists(newHostname)) {
        updateHostname(newHostname);
      } else {
        recoverFromHostnameError();
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

  private void recoverFromHostnameError() {
    final List<ServerInfo> serverInfoList =
        ConnectivityManager.getInstance(getApplicationContext()).getServerList();
    if (serverInfoList == null || serverInfoList.size() == 0) {
      LaunchUtil.showAddServerActivity(this);
      return;
    }

    // just connect to the first available
    final ServerInfo serverInfo = serverInfoList.get(0);

    rocketChatCache.setSelectedServerHostname(serverInfo.getHostname());
    rocketChatCache.setSelectedRoomId(null);
  }

  private void updateRoomIdIfNeeded(String newRoomId) {
    if (roomId == null) {
      if (newRoomId != null && assertRoomSubscriptionExists(newRoomId)) {
        updateRoomId(newRoomId);
      }
    } else {
      if (!roomId.equals(newRoomId) && assertRoomSubscriptionExists(newRoomId)) {
        updateRoomId(newRoomId);
      }
    }
  }

  private boolean assertRoomSubscriptionExists(String roomId) {
    if (!assertServerRealmStoreExists(hostname)) {
      return false;
    }

    RealmRoom room = RealmStore.get(hostname).executeTransactionForRead(realm ->
        realm.where(RealmRoom.class).equalTo(RealmRoom.ROOM_ID, roomId).findFirst());
    if (room == null) {
      rocketChatCache.setSelectedRoomId(null);
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

    subscribeToConfigChanges();

    ConnectivityManager.getInstance(getApplicationContext()).keepAliveServer();
    if (isNotification) {
      updateHostnameIfNeeded(rocketChatCache.getSelectedServerHostname());
      updateRoomIdIfNeeded(rocketChatCache.getSelectedRoomId());
      isNotification = false;
    }
  }

  @Override
  protected void onPause() {
    compositeDisposable.clear();

    super.onPause();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
  }

  private void subscribeToConfigChanges() {
    compositeDisposable.add(
        rocketChatCache.getSelectedServerHostnamePublisher()
            .map(Optional::get)
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::updateHostnameIfNeeded,
                Logger::report
            )
    );

    compositeDisposable.add(
        rocketChatCache.getSelectedRoomIdPublisher()
            .map(Optional::get)
            .map(this::convertStringToJsonObject)
            .map(jsonObject -> jsonObject.optString(rocketChatCache.getSelectedServerHostname(), null))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::updateRoomIdIfNeeded,
                Logger::report
            )
    );
  }

  private JSONObject convertStringToJsonObject(String json) throws JSONException {
    if (json == null) {
      return new JSONObject();
    }
    return new JSONObject(json);
  }
}
