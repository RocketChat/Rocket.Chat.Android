package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.fragment.chatroom.HomeFragment;
import chat.rocket.android.fragment.chatroom.RoomFragment;
import chat.rocket.android.fragment.sidebar.SidebarMainFragment;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmObjectObserver;
import chat.rocket.android.realm_helper.RealmStore;
import hugo.weaving.DebugLog;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class MainActivity extends AbstractAuthedActivity {

  private RealmObjectObserver<Session> sessionObserver;

  @Override protected int getLayoutContainerForFragment() {
    return R.id.activity_main_container;
  }

  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setupSidebar();
    if (roomId == null) {
      showFragment(new HomeFragment());
    }
  }

  private void setupSidebar() {
    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane != null) {
      final SlidingPaneLayout subPane = (SlidingPaneLayout) findViewById(R.id.sub_sliding_pane);
      pane.setPanelSlideListener(new SlidingPaneLayout.SimplePanelSlideListener() {
        @Override public void onPanelClosed(View panel) {
          super.onPanelClosed(panel);
          if (subPane != null) {
            subPane.closePane();
          }
        }
      });
    }
  }

  private boolean closeSidebarIfNeeded() {
    // REMARK: Tablet UI doesn't have SlidingPane!
    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane != null && pane.isOpen()) {
      pane.closePane();
      return true;
    }
    return false;
  }

  @DebugLog
  @Override protected void onServerConfigIdUpdated() {
    super.onServerConfigIdUpdated();
    updateSessionObserver();
    updateSidebarMainFragment();
  }

  private void updateSessionObserver() {
    if (sessionObserver != null) {
      sessionObserver.unsub();
      sessionObserver = null;
    }

    if (serverConfigId == null) {
      return;
    }

    RealmHelper realmHelper = RealmStore.get(serverConfigId);
    if (realmHelper == null) {
      return;
    }

    sessionObserver = realmHelper
        .createObjectObserver(realm ->
            realm.where(Session.class)
                .equalTo("sessionId", Session.DEFAULT_ID)
                .isNotNull("token")
                .equalTo("tokenVerified", true)
                .isNull("error"))
        .setOnUpdateListener(session -> {
          if (session == null) {
            LaunchUtil.showServerConfigActivity(this, serverConfigId);
          }
        });
    sessionObserver.sub();
  }

  private void updateSidebarMainFragment() {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.sidebar_fragment_container, SidebarMainFragment.create(serverConfigId))
        .commit();
  }

  @Override protected void onRoomIdUpdated() {
    super.onRoomIdUpdated();

    if (roomId != null) {
      showFragment(RoomFragment.create(serverConfigId, roomId));
      closeSidebarIfNeeded();
    } else {
      showFragment(new HomeFragment());
    }
  }

  @Override protected void onResume() {
    super.onResume();
    if (sessionObserver != null) {
      sessionObserver.keepalive();
    }
  }

  @Override protected void onDestroy() {
    if (sessionObserver != null) {
      sessionObserver.unsub();
      sessionObserver = null;
    }
    super.onDestroy();
  }

  @Override protected boolean onBackPress() {
    return closeSidebarIfNeeded() || super.onBackPress();
  }
}
