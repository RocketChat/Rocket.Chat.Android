package chat.rocket.android.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SlidingPaneLayout;
import android.support.v7.graphics.drawable.DrawerArrowDrawable;
import android.support.v7.widget.Toolbar;
import android.view.View;

import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.chatroom.HomeFragment;
import chat.rocket.android.fragment.chatroom.RoomFragment;
import chat.rocket.android.fragment.sidebar.SidebarMainFragment;
import chat.rocket.android.helper.LogcatIfError;
import chat.rocket.android.helper.TextUtils;
import chat.rocket.android.model.ServerConfig;
import chat.rocket.android.model.ddp.User;
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
  private boolean isForeground;
  private StatusTicker statusTicker;

  @Override
  protected int getLayoutContainerForFragment() {
    return R.id.activity_main_container;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    statusTicker = new StatusTicker();
    setupSidebar();
    if (roomId == null) {
      showFragment(new HomeFragment());
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    setUserOnlineIfServerAvailable();
  }

  @Override
  protected void onResume() {
    isForeground = true;
    super.onResume();
  }

  @Override
  protected void onPause() {
    isForeground = false;
    super.onPause();
  }

  @Override
  protected void onStop() {
    setUserAwayIfServerAvailable();

    super.onStop();
  }

  private void setUserOnlineIfServerAvailable() {
    if (serverConfigId != null) {
      new MethodCallHelper(this, serverConfigId).setUserPresence(User.STATUS_ONLINE)
          .continueWith(new LogcatIfError());
    }
  }

  private void setUserAwayIfServerAvailable() {
    if (serverConfigId != null) {
      new MethodCallHelper(this, serverConfigId).setUserPresence(User.STATUS_AWAY)
          .continueWith(new LogcatIfError());
    }
  }

  private void setupSidebar() {
    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane == null) {
      return;
    }

    final SlidingPaneLayout subPane = (SlidingPaneLayout) findViewById(R.id.sub_sliding_pane);
    pane.setPanelSlideListener(new SlidingPaneLayout.SimplePanelSlideListener() {
      @Override
      public void onPanelClosed(View panel) {
        super.onPanelClosed(panel);
        if (subPane != null) {
          subPane.closePane();
        }
      }
    });

    final DrawerArrowDrawable drawerArrowDrawable = new DrawerArrowDrawable(this);
    Toolbar toolbar = (Toolbar) findViewById(R.id.activity_main_toolbar);
    toolbar.setNavigationIcon(drawerArrowDrawable);
    toolbar.setNavigationOnClickListener(view -> {
      if (pane.isSlideable() && !pane.isOpen()) {
        pane.openPane();
      }
    });

    //ref: ActionBarDrawerToggle#setProgress
    pane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
      @Override
      public void onPanelSlide(View panel, float slideOffset) {
        drawerArrowDrawable.setProgress(slideOffset);
      }

      @Override
      public void onPanelOpened(View panel) {
        drawerArrowDrawable.setVerticalMirror(true);
      }

      @Override
      public void onPanelClosed(View panel) {
        drawerArrowDrawable.setVerticalMirror(false);
      }
    });
  }

  private boolean closeSidebarIfNeeded() {
    // REMARK: Tablet UI doesn't have SlidingPane!
    SlidingPaneLayout pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    if (pane != null && pane.isSlideable() && pane.isOpen()) {
      pane.closePane();
      return true;
    }
    return false;
  }

  @DebugLog
  @Override
  protected void onServerConfigIdUpdated() {
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
            Session.queryDefaultSession(realm)
                .isNotNull(Session.TOKEN))
        .setOnUpdateListener(this::onSessionChanged);
    sessionObserver.sub();
  }

  private void onSessionChanged(@Nullable Session session) {
    if (session == null) {
      if (isForeground) {
        LaunchUtil.showLoginActivity(this, serverConfigId);
      }
      statusTicker.updateStatus(StatusTicker.STATUS_DISMISS, null);
    } else if (!TextUtils.isEmpty(session.getError())) {
      statusTicker.updateStatus(StatusTicker.STATUS_CONNECTION_ERROR,
          Snackbar.make(findViewById(getLayoutContainerForFragment()),
              R.string.fragment_retry_login_error_title, Snackbar.LENGTH_INDEFINITE)
              .setAction(R.string.fragment_retry_login_retry_title, view ->
                  RealmStore.getDefault()
                      .executeTransaction(realm -> {
                        ServerConfig config = realm.where(ServerConfig.class)
                            .equalTo(ServerConfig.ID, serverConfigId).findFirst();

                        if (config != null
                            && config.getState() == ServerConfig.STATE_CONNECTION_ERROR) {
                          config.setState(ServerConfig.STATE_READY);
                        }
                        return null;
                      }).continueWith(new LogcatIfError())));
    } else if (!session.isTokenVerified()) {
      statusTicker.updateStatus(StatusTicker.STATUS_TOKEN_LOGIN,
          Snackbar.make(findViewById(getLayoutContainerForFragment()),
              R.string.server_config_activity_authenticating, Snackbar.LENGTH_INDEFINITE));
    } else {
      statusTicker.updateStatus(StatusTicker.STATUS_DISMISS, null);
    }
  }

  private void updateSidebarMainFragment() {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.sidebar_fragment_container, SidebarMainFragment.create(serverConfigId))
        .commit();
  }

  @Override
  protected void onRoomIdUpdated() {
    super.onRoomIdUpdated();

    if (roomId != null && RoomFragment.canCreate(RealmStore.get(serverConfigId))) {
      showFragment(RoomFragment.create(serverConfigId, roomId));
      closeSidebarIfNeeded();
    } else {
      showFragment(new HomeFragment());
    }
  }

  @Override
  protected void onDestroy() {
    if (sessionObserver != null) {
      sessionObserver.unsub();
      sessionObserver = null;
    }
    super.onDestroy();
  }

  @Override
  protected boolean onBackPress() {
    return closeSidebarIfNeeded() || super.onBackPress();
  }

  //TODO: consider this class to define in layouthelper for more complicated operation.
  private static class StatusTicker {
    public static final int STATUS_DISMISS = 0;
    public static final int STATUS_CONNECTION_ERROR = 1;
    public static final int STATUS_TOKEN_LOGIN = 2;

    private int status;
    private Snackbar snackbar;

    public StatusTicker() {
      status = STATUS_DISMISS;
    }

    public void updateStatus(int status, Snackbar snackbar) {
      if (status == this.status) {
        return;
      }
      this.status = status;
      if (this.snackbar != null) {
        this.snackbar.dismiss();
      }
      if (status != STATUS_DISMISS) {
        this.snackbar = snackbar;
        if (this.snackbar != null) {
          this.snackbar.show();
        }
      }
    }
  }
}
