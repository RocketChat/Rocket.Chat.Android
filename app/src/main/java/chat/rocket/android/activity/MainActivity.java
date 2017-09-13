package chat.rocket.android.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import chat.rocket.android.LaunchUtil;
import chat.rocket.android.R;
import chat.rocket.android.RocketChatCache;
import chat.rocket.android.api.MethodCallHelper;
import chat.rocket.android.fragment.chatroom.HomeFragment;
import chat.rocket.android.fragment.chatroom.RoomFragment;
import chat.rocket.android.fragment.sidebar.SidebarMainFragment;
import chat.rocket.android.helper.KeyboardHelper;
import chat.rocket.android.service.ConnectivityManager;
import chat.rocket.android.widget.RoomToolbar;
import chat.rocket.android.widget.helper.FrescoHelper;
import chat.rocket.core.interactors.CanCreateRoomInteractor;
import chat.rocket.core.interactors.RoomInteractor;
import chat.rocket.core.interactors.SessionInteractor;
import chat.rocket.core.repositories.PublicSettingRepository;
import chat.rocket.core.utils.Pair;
import chat.rocket.persistence.realm.repositories.RealmPublicSettingRepository;
import chat.rocket.persistence.realm.repositories.RealmRoomRepository;
import chat.rocket.persistence.realm.repositories.RealmSessionRepository;
import chat.rocket.persistence.realm.repositories.RealmUserRepository;
import hugo.weaving.DebugLog;

/**
 * Entry-point for Rocket.Chat.Android application.
 */
public class MainActivity extends AbstractAuthedActivity implements MainContract.View {
  private RoomToolbar toolbar;
  private StatusTicker statusTicker;
  private SlidingPaneLayout pane;
  private MainContract.Presenter presenter;

  @Override
  protected int getLayoutContainerForFragment() {
    return R.id.activity_main_container;
  }

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    toolbar = (RoomToolbar) findViewById(R.id.activity_main_toolbar);
    statusTicker = new StatusTicker();
    pane = (SlidingPaneLayout) findViewById(R.id.sliding_pane);
    setupToolbar();
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (hostname == null || presenter == null) {
      hostname = new RocketChatCache(getApplicationContext()).getSelectedServerHostname();
      if (hostname == null) {
        showAddServerScreen();
      } else {
        onHostnameUpdated();
      }
    } else {
      presenter.bindViewOnly(this);
      presenter.loadSignedInServers(hostname);
    }
  }

  @Override
  protected void onPause() {
    if (presenter != null) {
      presenter.release();
    }

    super.onPause();
  }

  private void showAddServerActivity() {
    Intent intent = new Intent(this, AddServerActivity.class);
    intent.putExtra(AddServerActivity.EXTRA_FINISH_ON_BACK_PRESS, true);
    startActivity(intent);
  }

  private void setupToolbar() {
    pane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
      @Override
      public void onPanelSlide(View view, float v) {
        //Ref: ActionBarDrawerToggle#setProgress
        toolbar.setNavigationIconProgress(v);
      }

      @Override
      public void onPanelOpened(View view) {
        toolbar.setNavigationIconVerticalMirror(true);
      }

      @Override
      public void onPanelClosed(View view) {
        toolbar.setNavigationIconVerticalMirror(false);
        Fragment fragment = getSupportFragmentManager()
                .findFragmentById(R.id.sidebar_fragment_container);
        if (fragment != null && fragment instanceof SidebarMainFragment) {
          SidebarMainFragment sidebarMainFragment = (SidebarMainFragment) fragment;
          sidebarMainFragment.toggleUserActionContainer(false);
          sidebarMainFragment.showUserActionContainer(false);
        }
      }
    });

    toolbar.setNavigationOnClickListener(view -> {
      if (pane.isSlideable() && !pane.isOpen()) {
        pane.openPane();
      }
    });
  }

  private boolean closeSidebarIfNeeded() {
    // REMARK: Tablet UI doesn't have SlidingPane!
    if (pane != null && pane.isSlideable() && pane.isOpen()) {
      pane.closePane();
      return true;
    }
    return false;
  }

  @DebugLog
  @Override
  protected void onHostnameUpdated() {
    super.onHostnameUpdated();

    if (presenter != null) {
      presenter.release();
    }

    RoomInteractor roomInteractor = new RoomInteractor(new RealmRoomRepository(hostname));

    CanCreateRoomInteractor createRoomInteractor = new CanCreateRoomInteractor(
        new RealmUserRepository(hostname),
        new SessionInteractor(new RealmSessionRepository(hostname))
    );

    SessionInteractor sessionInteractor = new SessionInteractor(
        new RealmSessionRepository(hostname)
    );

    PublicSettingRepository publicSettingRepository = new RealmPublicSettingRepository(hostname);

    presenter = new MainPresenter(
        roomInteractor,
        createRoomInteractor,
        sessionInteractor,
        new MethodCallHelper(this, hostname),
        ConnectivityManager.getInstance(getApplicationContext()),
        new RocketChatCache(this),
        publicSettingRepository
    );

    updateSidebarMainFragment();

    presenter.bindView(this);
  }

  private void updateSidebarMainFragment() {
    closeSidebarIfNeeded();
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.sidebar_fragment_container, SidebarMainFragment.create(hostname))
        .commit();
  }

  @Override
  protected void onRoomIdUpdated() {
    super.onRoomIdUpdated();
    presenter.onOpenRoom(hostname, roomId);
  }

  @Override
  protected boolean onBackPress() {
    return closeSidebarIfNeeded() || super.onBackPress();
  }

  @Override
  public void showHome() {
    showFragment(new HomeFragment());
  }

  @Override
  public void showRoom(String hostname, String roomId) {
    showFragment(RoomFragment.create(hostname, roomId));
    closeSidebarIfNeeded();
    KeyboardHelper.hideSoftKeyboard(this);
  }

  @Override
  public void showUnreadCount(long roomsCount, int mentionsCount) {
      toolbar.setUnreadBadge((int) roomsCount, mentionsCount);
  }

  @Override
  public void showAddServerScreen() {
    LaunchUtil.showAddServerActivity(this);
  }

  @Override
  public void showLoginScreen() {
    LaunchUtil.showLoginActivity(this, hostname);
    statusTicker.updateStatus(StatusTicker.STATUS_DISMISS, null);
  }

  @Override
  public void showConnectionError() {
    statusTicker.updateStatus(StatusTicker.STATUS_CONNECTION_ERROR,
        Snackbar.make(findViewById(getLayoutContainerForFragment()),
            R.string.fragment_retry_login_error_title, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.fragment_retry_login_retry_title, view ->
                presenter.onRetryLogin()));
  }

  @Override
  public void showConnecting() {
    statusTicker.updateStatus(StatusTicker.STATUS_TOKEN_LOGIN,
        Snackbar.make(findViewById(getLayoutContainerForFragment()),
            R.string.server_config_activity_authenticating, Snackbar.LENGTH_INDEFINITE));
  }

  @Override
  public void showConnectionOk() {
    statusTicker.updateStatus(StatusTicker.STATUS_DISMISS, null);
  }

  @Override
  public void showSignedInServers(List<Pair<String, Pair<String, String>>> serverList) {
    final SlidingPaneLayout subPane = (SlidingPaneLayout) findViewById(R.id.sub_sliding_pane);
    if (subPane != null) {
      LinearLayout serverListContainer = subPane.findViewById(R.id.server_list_bar);
      View addServerButton = subPane.findViewById(R.id.btn_add_server);
      addServerButton.setOnClickListener(view -> showAddServerActivity());
      for (Pair<String, Pair<String, String>> server : serverList) {
        String serverHostname = server.first;
        Pair<String, String> serverInfoPair = server.second;
        String logoUrl = serverInfoPair.first;
        String siteName = serverInfoPair.second;
        if (serverListContainer.findViewWithTag(serverHostname) == null) {
          int serverCount = serverListContainer.getChildCount();

          View serverRow = LayoutInflater.from(this).inflate(R.layout.server_row, serverListContainer, false);
          SimpleDraweeView serverButton = serverRow.findViewById(R.id.drawee_server_button);
          TextView hostnameLabel = serverRow.findViewById(R.id.text_view_server_label);
          TextView siteNameLabel = serverRow.findViewById(R.id.text_view_site_name_label);
          ImageView dotView = serverRow.findViewById(R.id.selected_server_dot);

          serverButton.setTag(serverHostname);
          hostnameLabel.setText(serverHostname);
          siteNameLabel.setText(siteName);

          // Currently selected server
          if (serverHostname.equalsIgnoreCase(hostname)) {
            serverRow.setSelected(true);
            dotView.setVisibility(View.VISIBLE);
          } else {
            dotView.setVisibility(View.GONE);
          }

          serverRow.setOnClickListener(view -> changeServerIfNeeded(serverHostname));

          FrescoHelper.INSTANCE.loadImage(serverButton, logoUrl, ContextCompat.getDrawable(this, R.mipmap.ic_launcher));

          serverListContainer.addView(serverRow, serverCount - 1);
        }
      }
    }
  }

  private void changeServerIfNeeded(String serverHostname) {
    if (!hostname.equalsIgnoreCase(serverHostname)) {
      RocketChatCache rocketChatCache = new RocketChatCache(getApplicationContext());
      rocketChatCache.setSelectedServerHostname(serverHostname);
      recreate();
    }
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
