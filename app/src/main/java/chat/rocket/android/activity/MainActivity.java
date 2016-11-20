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
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;
import hugo.weaving.DebugLog;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class MainActivity extends AbstractAuthedActivity {
  @Override protected int getLayoutContainerForFragment() {
    return R.id.activity_main_container;
  }


  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    setupSidebar();
    showFragment(new HomeFragment());
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

  private void closeSidebarIfNeeded() {
    // REMARK: Tablet UI doesn't have SlidingPane!
    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane != null) {
      pane.closePane();
    }
  }

  @DebugLog
  @Override protected void onServerConfigIdUpdated() {
    super.onServerConfigIdUpdated();

    getSupportFragmentManager().beginTransaction()
        .replace(R.id.sidebar_fragment_container, SidebarMainFragment.create(serverConfigId))
        .commit();

    if (serverConfigId == null) {
      return;
    }

    RealmHelper realmHelper = RealmStore.get(serverConfigId);
    if (realmHelper == null) {
      return;
    }

    Session session = realmHelper.executeTransactionForRead(realm ->
        realm.where(Session.class).equalTo("sessionId", Session.DEFAULT_ID).findFirst());

    if (session != null
        && !TextUtils.isEmpty(session.getToken())
        && session.isTokenVerified()
        && TextUtils.isEmpty(session.getError())) {
      // session is OK.
    } else {
      LaunchUtil.showServerConfigActivity(this, serverConfigId);
    }
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


}
