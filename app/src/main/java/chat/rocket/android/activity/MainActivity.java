package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.fragment.chatroom.HomeFragment;
import chat.rocket.android.fragment.chatroom.RoomFragment;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.layouthelper.chatroom.RoomListManager;
import chat.rocket.android.model.internal.Session;
import chat.rocket.android.realm_helper.RealmHelper;
import chat.rocket.android.realm_helper.RealmStore;
import com.jakewharton.rxbinding.view.RxView;
import com.jakewharton.rxbinding.widget.RxCompoundButton;
import hugo.weaving.DebugLog;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class MainActivity extends AbstractAuthedActivity {
  private RoomListManager roomListManager;

  @Override protected int getLayoutContainerForFragment() {
    return R.id.activity_main_container;
  }


  @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    if (savedInstanceState == null) {
      showFragment(new HomeFragment());
    }

    setupSidebar();
  }

  private void setupSidebar() {
    setupUserActionToggle();

    roomListManager = new RoomListManager(
        (LinearLayout) findViewById(R.id.channels_container),
        (LinearLayout) findViewById(R.id.direct_messages_container));
    roomListManager.setOnItemClickListener(view -> {
      showRoomFragment(view.getRoomId());
      closeSidebarIfNeeded();
    });

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

  private void setupUserActionToggle() {
    final CompoundButton toggleUserAction =
        ((CompoundButton) findViewById(R.id.toggle_user_action));
    toggleUserAction.setFocusableInTouchMode(false);
    findViewById(R.id.user_info_container).setOnClickListener(view -> {
      toggleUserAction.toggle();
    });
    RxCompoundButton.checkedChanges(toggleUserAction)
        .compose(bindToLifecycle())
        .subscribe(RxView.visibility(findViewById(R.id.user_action_outer_container)));
  }

  private void showRoomFragment(String roomId) {
    showFragment(RoomFragment.create(serverConfigId, roomId));
  }

  @DebugLog
  @Override protected void onServerConfigIdUpdated() {
    super.onServerConfigIdUpdated();

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
}
