package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hadisatrio.optional.Optional;

import java.util.List;

import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.helper.Logger;
import chat.rocket.android.push.PushManager;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.core.models.ServerInfo;
import chat.rocket.persistence.realm.RealmStore;
import chat.rocket.persistence.realm.models.ddp.RealmRoom;
import icepick.State;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import okhttp3.HttpUrl;

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

    if (intent.hasExtra(PushManager.EXTRA_HOSTNAME)) {
      String hostname = intent.getStringExtra(PushManager.EXTRA_HOSTNAME);
      HttpUrl url = HttpUrl.parse(hostname);
      if (url != null) {
        String hostnameFromPush = url.host();
        String loginHostname = rocketChatCache.getSiteUrlFor(hostnameFromPush);
        rocketChatCache.setSelectedServerHostname(loginHostname);

        if (intent.hasExtra(PushManager.EXTRA_ROOM_ID)) {
          rocketChatCache.setSelectedRoomId(intent.getStringExtra(PushManager.EXTRA_ROOM_ID));
        }
      }
      PushManager.INSTANCE.clearNotificationsByHost(hostname);
    } else {
      updateHostnameIfNeeded(rocketChatCache.getSelectedServerHostname());
    }

    if (intent.hasExtra(PushManager.EXTRA_NOT_ID) && intent.hasExtra(PushManager.EXTRA_HOSTNAME)) {
      isNotification = true;
      int notificationId = intent.getIntExtra(PushManager.EXTRA_NOT_ID, 0);
      String hostname = intent.getStringExtra(PushManager.EXTRA_HOSTNAME);
      HttpUrl url = HttpUrl.parse(hostname);
      if (url != null) {
        String hostnameFromPush = url.host();
        String loginHostname = rocketChatCache.getSiteUrlFor(hostnameFromPush);
        PushManager.INSTANCE.clearNotificationsByHostAndNotificationId(loginHostname, notificationId);
      } else {
        PushManager.INSTANCE.clearNotificationsByNotificationId(notificationId);
      }

    }
  }

  private void updateHostnameIfNeeded(String newHostname) {
    if (hostname == null) {
      if (newHostname != null && assertServerRealmStoreExists(newHostname)) {
        updateHostname(newHostname);
        updateRoomIdIfNeeded(rocketChatCache.getSelectedRoomId());
      } else {
        recoverFromHostnameError();
      }
    } else {
      if (hostname.equals(newHostname)) {
        updateHostname(newHostname);
        updateRoomIdIfNeeded(rocketChatCache.getSelectedRoomId());
        return;
      }

      if (assertServerRealmStoreExists(newHostname)) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
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
            .filter(Optional::isPresent)
            .map(Optional::get)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                this::updateRoomIdIfNeeded,
                Logger::report
            )
    );
  }
}
