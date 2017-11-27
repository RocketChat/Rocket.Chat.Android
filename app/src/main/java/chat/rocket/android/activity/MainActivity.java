package chat.rocket.android.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import chat.rocket.android.service.ConnectivityManagerApi;
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
    private SlidingPaneLayout pane;
    private MainContract.Presenter presenter;
    private volatile Snackbar statusTicker;

    @Override
    public int getLayoutContainerForFragment() {
        return R.id.activity_main_container;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.activity_main_toolbar);
        pane = findViewById(R.id.sliding_pane);
        setupToolbar();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ConnectivityManagerApi connectivityManager = ConnectivityManager.getInstance(getApplicationContext());
        if (hostname == null || presenter == null) {
            String previousHostname = hostname;
            hostname = new RocketChatCache(getApplicationContext()).getSelectedServerHostname();
            if (hostname == null) {
                showAddServerScreen();
            } else {
                onHostnameUpdated();
                if (!hostname.equalsIgnoreCase(previousHostname)) {
                    connectivityManager.resetConnectivityStateList();
                    connectivityManager.keepAliveServer();
                }
            }
        } else {
            connectivityManager.keepAliveServer();
            presenter.bindView(this);
            presenter.loadSignedInServers(hostname);
            roomId = new RocketChatCache(getApplicationContext()).getSelectedRoomId();
        }
    }

    @Override
    protected void onPause() {
        if (presenter != null) {
            presenter.release();
        }
        // Dismiss any status ticker
        if (statusTicker != null) statusTicker.dismiss();

        super.onPause();
    }

    private void showAddServerActivity() {
        closeSidebarIfNeeded();
        Intent intent = new Intent(this, AddServerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(AddServerActivity.EXTRA_FINISH_ON_BACK_PRESS, true);
        startActivity(intent);
    }

    private void setupToolbar() {
        if (pane != null) {
            pane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(@NonNull View view, float v) {
                    //Ref: ActionBarDrawerToggle#setProgress
                    toolbar.setNavigationIconProgress(v);
                }

                @Override
                public void onPanelOpened(@NonNull View view) {
                    toolbar.setNavigationIconVerticalMirror(true);
                }

                @Override
                public void onPanelClosed(@NonNull View view) {
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

            if (toolbar != null) {
                toolbar.setNavigationOnClickListener(view -> {
                    if (pane.isSlideable() && !pane.isOpen()) {
                        pane.openPane();
                    }
                });
            }
        }
        closeSidebarIfNeeded();
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

        RocketChatCache rocketChatCache = new RocketChatCache(this);

        presenter = new MainPresenter(
                roomInteractor,
                createRoomInteractor,
                sessionInteractor,
                new MethodCallHelper(this, hostname),
                ConnectivityManager.getInstance(getApplicationContext()),
                rocketChatCache,
                publicSettingRepository
        );

        updateSidebarMainFragment();

        presenter.bindView(this);
        presenter.loadSignedInServers(hostname);

        roomId = rocketChatCache.getSelectedRoomId();
    }

    private void updateSidebarMainFragment() {
        closeSidebarIfNeeded();
        String selectedServerHostname = new RocketChatCache(this).getSelectedServerHostname();
        Fragment sidebarFragment = findFragmentByTag(selectedServerHostname);
        if (sidebarFragment == null) {
            sidebarFragment = SidebarMainFragment.create(selectedServerHostname);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.sidebar_fragment_container, sidebarFragment, selectedServerHostname)
                .commit();
        getSupportFragmentManager().executePendingTransactions();
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
        showConnectionOk();
    }

    @Override
    public synchronized void showConnectionError() {
        dismissStatusTickerIfShowing();
        statusTicker = Snackbar.make(findViewById(getLayoutContainerForFragment()),
                R.string.fragment_retry_login_error_title, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.fragment_retry_login_retry_title, view ->
                        ConnectivityManager.getInstance(getApplicationContext()).keepAliveServer());
        statusTicker.show();
    }

    @Override
    public synchronized void showConnecting() {
        dismissStatusTickerIfShowing();
        statusTicker = Snackbar.make(findViewById(getLayoutContainerForFragment()),
                R.string.server_config_activity_authenticating, Snackbar.LENGTH_INDEFINITE);
        statusTicker.show();
    }

    @Override
    public synchronized void showConnectionOk() {
        dismissStatusTickerIfShowing();
    }

    private void dismissStatusTickerIfShowing() {
        if (statusTicker != null) {
            statusTicker.dismiss();
        }
    }

    @Override
    public void showSignedInServers(List<Pair<String, Pair<String, String>>> serverList) {
        final SlidingPaneLayout subPane = findViewById(R.id.sub_sliding_pane);
        if (subPane != null) {
            LinearLayout serverListContainer = subPane.findViewById(R.id.server_list_bar);
            View addServerButton = subPane.findViewById(R.id.btn_add_server);
            addServerButton.setOnClickListener(view -> showAddServerActivity());
            serverListContainer.removeAllViews();
            for (Pair<String, Pair<String, String>> server : serverList) {
                String serverHostname = server.first;
                Pair<String, String> serverInfoPair = server.second;
                String logoUrl = serverInfoPair.first;
                String siteName = serverInfoPair.second;
                View serverView = serverListContainer.findViewWithTag(serverHostname);
                if (serverView == null) {
                    View newServerView = LayoutInflater.from(this).inflate(R.layout.server_row, serverListContainer, false);
                    SimpleDraweeView serverButton = newServerView.findViewById(R.id.drawee_server_button);
                    TextView hostnameLabel = newServerView.findViewById(R.id.text_view_server_label);
                    TextView siteNameLabel = newServerView.findViewById(R.id.text_view_site_name_label);
                    ImageView dotView = newServerView.findViewById(R.id.selected_server_dot);

                    newServerView.setTag(serverHostname);
                    hostnameLabel.setText(serverHostname);
                    siteNameLabel.setText(siteName);

                    // Currently selected server
                    if (hostname.equalsIgnoreCase(serverHostname)) {
                        newServerView.setSelected(true);
                        dotView.setVisibility(View.VISIBLE);
                    } else {
                        newServerView.setSelected(false);
                        dotView.setVisibility(View.GONE);
                    }

                    newServerView.setOnClickListener(view -> changeServerIfNeeded(serverHostname));

                    Drawable drawable = ContextCompat.getDrawable(this, R.mipmap.ic_launcher);
                    if (drawable == null) {
                        int id = getResources().getIdentifier(
                                "rocket_chat_notification", "drawable", getPackageName());
                        drawable = ContextCompat.getDrawable(this, id);
                    }
                    FrescoHelper.INSTANCE.loadImage(serverButton, logoUrl, drawable);

                    serverListContainer.addView(newServerView);
                }
            }
            serverListContainer.addView(addServerButton);
        }
    }

    @Override
    public void refreshRoom() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(getLayoutContainerForFragment());
        if (fragment != null && fragment instanceof RoomFragment) {
            RoomFragment roomFragment = (RoomFragment) fragment;
            roomFragment.loadMessages();
        }
    }

    private void changeServerIfNeeded(String serverHostname) {
        if (!hostname.equalsIgnoreCase(serverHostname)) {
            RocketChatCache rocketChatCache = new RocketChatCache(getApplicationContext());
            rocketChatCache.setSelectedServerHostname(serverHostname);
        }
    }

    @DebugLog
    public void onLogout() {
        if (new RocketChatCache(getApplicationContext()).getSelectedServerHostname() == null) {
            LaunchUtil.showMainActivity(this);
        } else {
            onHostnameUpdated();
        }
    }

    @DebugLog
    public void beforeLogoutCleanUp() {
        presenter.beforeLogoutCleanUp();
    }
}
